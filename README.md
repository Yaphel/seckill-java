## 介绍

java实现的秒杀网站，基于springboot。

## 演示地址

http://118.195.147.254:/8888

注：在输入手机号时，随便输入一个11位的数字即可。

## 技术栈

- spring boot
- mybatis
- redis/mysql
- rocketmq
- thymeleaf/bootstrap
- nginx

## 架构图

 ![image-20210424131956845](/Users/shihaonan/Library/Application Support/typora-user-images/image-20210424131956845.png)

## 秒杀流程

秒杀进行的过程包含以下两步骤：

步骤一（秒杀）：这个步骤用户并发量非常大，数据存在redis中。抢到后，有30分钟的时间等待用户付款， 如果用户过期未付款，则Redis库存加1 ，用户自动放弃付款。（流程图见下）

步骤二（付款）：用户付款成功后，后台把付款记录持久化到MySQL中，这个步骤的并发就要小很多。这部分不在本工程中实现。

### 流程图

![image-20210424132021946](/Users/shihaonan/Library/Application Support/typora-user-images/image-20210424132021946.png)

### 过程解释

1.先经过Nginx负载均衡;

2.Nginx里面通过配置文件配置限流功能，限流算法是漏桶法;

3.Redis判断是否秒杀过。避免重复秒杀。如果没有秒杀过把手机号和seckillId封装成一条消息发送到MQ，请求变成被顺序串行处理立即返回状态“排队中”到客户端上，客户端上回显示“排队中...”

4.后台监听MQ里消息，每次取一条消息，并解析后，请求Redis做库存减1操作（decr命令）并手动ACK队列 如果减库存成功，则在Redis里记录下库存成功的用户手机号userPhone.

5.流程图Step2：客户端排队成功后，定时请求后台查询是否秒杀成功，后面会去查询Redis是否秒杀成功
如果抢购成功，或者抢购失败则停止定时查询， 如果是排队中，则继续定时查询。

## 部署

1.在properties文件中修改配置信息。将工程打成jar包。

2.配置好redis/mysql/rocketmq。

3.将jar包运行在服务器上即可。

## 部分功能实现

### 限流

>
>
>```java
>/**
> * 秒杀前的限流.
> * 使用了Google guava的RateLimiter
> */
>@Service
>public class AccessLimitServiceImpl implements AccessLimitService {
>    /**
>     * 每秒钟只发出10个令牌，拿到令牌的请求才可以进入秒杀过程
>     */
>    private RateLimiter seckillRateLimiter = RateLimiter.create(10);
>
>    /**
>     * 尝试获取令牌
>     * @return
>     */
>    @Override
>    public boolean tryAcquireSeckill() {
>        return seckillRateLimiter.tryAcquire();
>    }
>}
>```

