package com.seckill.backend.mq;


import com.alibaba.fastjson.JSON;
import com.seckill.backend.dto.SeckillMsgBody;
import com.seckill.backend.enums.AckAction;
import com.seckill.backend.enums.SeckillStateEnum;
import com.seckill.backend.exception.SeckillException;
import com.seckill.backend.service.SeckillService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

//关于rocketmq和rabbitmq的连接方式的区别，有篇文章
//https://www.cnblogs.com/guazi/p/6664984.html
//大意是，rabbitmq使用的连接不是线程安全的，因此需要分线程。
//而rocketmq底层用的netty的长连接，这个东西线程安全。既然线程安全，那么只用一个连接就可以了。

@Component
public class MQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MQConsumer.class);

    @Value("${mq.consumer.group}")
    private String group;
    @Value("${mq.consumer.nameSrv}")
    private String nameSrv;
    @Value("${mq.consumer.topic}")
    private String topicName;
    @PostConstruct
    public void init(){
        initMQConsumer(group,nameSrv,topicName);
    }
    private DefaultMQPushConsumer consumer = null;
    /**
     接受handler，并在不同服务器复写handler。
     **/
    public void initMQConsumer(String group, String nameSrvAddr, String topicName) {
        consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(nameSrvAddr+":9876");
        try {
            consumer.subscribe(topicName, "*");
            consumer.registerMessageListener(new MessageListenerConcurrently() {

                @Override
                public ConsumeConcurrentlyStatus consumeMessage(
                        List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        try {
                            String str=new String(msg.getBody());
                            receive(str);
                        }catch (Exception e){
                            continue;
                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public void shutDownConsumer() {
        if (consumer != null) {
            consumer.shutdown();
        }
    }

    @Resource
    private SeckillService seckillService;


    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;


    public void receive(String message) {
        try {
            handleDelivery(message);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void handleDelivery(String msg)
            throws IOException {

        logger.info("[mqReceive]  '" + msg + "'");
        SeckillMsgBody msgBody = JSON.parseObject(msg, SeckillMsgBody.class);

        AckAction ackAction = AckAction.ACCEPT;
        try {
            // 这里演延时2秒，模式秒杀的耗时操作, 上线的时候需要注释掉
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    logger.error(e.getMessage(), e);
//                }
            seckillService.handleInRedis(msgBody.getSeckillId(), msgBody.getUserPhone());
            ackAction = AckAction.ACCEPT;
        } catch (SeckillException seckillE) {
            if (seckillE.getSeckillStateEnum() == SeckillStateEnum.SOLD_OUT
                    || seckillE.getSeckillStateEnum() == SeckillStateEnum.REPEAT_KILL) {
                // 已售罄，或者此人之前已经秒杀过的
                ackAction = AckAction.THROW;
            } else {
                logger.error(seckillE.getMessage(), seckillE);
                logger.info("---->NACK--error_requeue!!!");
                ackAction = AckAction.RETRY;
            }
        } finally {
            logger.info("------processIt----");
//            switch (ackAction) {
//                case ACCEPT:
//                    try {
//                        logger.info("---->ACK");
//                        channel.basicAck(envelope.getDeliveryTag(), false);
//                    } catch (IOException ioE) {
//                        logger.info("---------basicAck_throws_IOException----------");
//                        logger.error(ioE.getMessage(), ioE);
//                        throw ioE;
//                    }
//
//                    Jedis jedis = jedisPool.getResource();
//                    jedis.srem(RedisKey.QUEUE_PRE_SECKILL, msgBody.getSeckillId() + "@" + msgBody.getUserPhone());
//                    jedis.close();
//                    break;
//
//                case THROW:
//                    logger.info("--LET_MQ_ACK REASON:SeckillStateEnum.SOLD_OUT,SeckillStateEnum.REPEAT_KILL");
//                    channel.basicAck(envelope.getDeliveryTag(), false);
//
//                    Jedis jedis1 = jedisPool.getResource();
//                    jedis1.srem(RedisKey.QUEUE_PRE_SECKILL, msgBody.getSeckillId() + "@" + msgBody.getUserPhone());
//                    jedis1.close();
//
//                    break;
//
//                case RETRY:
//                    logger.info("---->NACK--error_requeue!!!");
//                    channel.basicNack(envelope.getDeliveryTag(), false, true);
//                    break;

//           }
        }
    }

}
