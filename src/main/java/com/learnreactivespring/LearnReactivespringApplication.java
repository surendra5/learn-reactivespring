package com.learnreactivespring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class LearnReactivespringApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearnReactivespringApplication.class, args);
        System.out.println("main class");
    }

}
