package com.seckill.backend.dao;

import com.seckill.backend.entity.Seckill;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SeckillDAO {

    /**
     * 减库存,
     *
     * @param seckillId
     * @param killTime
     * @return 如果影响行数>1，表示更新的记录行数
     */
    int reduceInventory(@Param("seckillId") long seckillId, @Param("oldVersion") long oldVersion,
                        @Param("newVersion") long newVersion);

    /**
     * 根据id查询秒杀对象
     *
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);

    /**
     * 根据偏移量查询秒杀商品列表
     *
     * @param offet
     * @param limit
     * @return
     */
    List<Seckill> queryAll(@Param("offset") int offet, @Param("limit") int limit);

    /**
     * 使用存储过程执行秒杀
     *
     * @param paramMap
     */
    void killByProcedure(Map<String, Object> paramMap);

}
