package com.github.vevc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author vevc
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class JavaXahApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaXahApplication.class, args);
    }
}
