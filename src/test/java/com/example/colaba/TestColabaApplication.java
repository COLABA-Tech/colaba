package com.example.colaba;

import org.springframework.boot.SpringApplication;

public class TestColabaApplication {

    public static void main(String[] args) {
        SpringApplication.from(ColabaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
