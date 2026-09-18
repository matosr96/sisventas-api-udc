package com.api.sisventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SisventasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SisventasApplication.class, args);
    }

}
