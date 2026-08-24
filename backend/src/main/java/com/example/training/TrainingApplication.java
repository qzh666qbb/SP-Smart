package com.example.training;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan({"com.example.training.*.infrastructure.persistence", "com.example.training.*.infrastructure.mapper"})
@EnableCaching
@EnableScheduling
@EnableAsync
public class TrainingApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TrainingApplication.class, args);
    }
}
