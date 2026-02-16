package com.securemarts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SecuremartsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecuremartsApplication.class, args);
    }
}
