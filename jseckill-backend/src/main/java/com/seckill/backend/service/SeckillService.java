package com.seckill.backend.service;

import com.seckill.backend.dto.Exposer;
import com.seckill.backend.dto.SeckillExecution;
import com.seckill.backend.entity.Seckill;
import com.seckill.backend.exception.SeckillException;

import java.util.List;

public interface SeckillService {

    /**
     * 查询所有秒杀记录
     *
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     *
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启输出秒杀接口地址,
     * 否则输出系统时间和秒杀时间
     *
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException;

    SeckillExecution updateInventory(long seckillId, long userPhone)
            throws SeckillException;

    /**
     * 在Redis中真正进行秒杀操作
     * @param seckillId
     * @param userPhone
     * @throws SeckillException
     */
    void handleInRedis(long seckillId, long userPhone) throws SeckillException;

    public int isGrab(long seckillId, long userPhone);
}