package com.seckill.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.seckill.backend.dao")
public class JseckillBackendApp {

    public static void main(String[] args) {
        SpringApplication.run(JseckillBackendApp.class, args);
    }
}
