package com.wyt.agriculture;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.wyt.agriculture.mapper")
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@Slf4j
public class AgriculturalProductsMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriculturalProductsMarketApplication.class, args);
    }

}
