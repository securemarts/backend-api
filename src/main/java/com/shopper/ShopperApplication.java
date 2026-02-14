package com.shopper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ShopperApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopperApplication.class, args);
    }
}
