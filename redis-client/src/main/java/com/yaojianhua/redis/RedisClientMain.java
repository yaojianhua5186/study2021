package com.yaojianhua.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisClientMain {
    public static void main(String[] args) {
        SpringApplication.run(RedisClientMain.class,args);
    }
}
