package com.seckill.backend.exception;

import com.seckill.backend.enums.SeckillStateEnum;

public class SeckillException extends RuntimeException {

    private SeckillStateEnum seckillStateEnum;

    public SeckillException(SeckillStateEnum seckillStateEnum) {
        this.seckillStateEnum = seckillStateEnum;
    }

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeckillStateEnum getSeckillStateEnum() {
        return seckillStateEnum;
    }

    public void setSeckillStateEnum(SeckillStateEnum seckillStateEnum) {
        this.seckillStateEnum = seckillStateEnum;
    }
}
