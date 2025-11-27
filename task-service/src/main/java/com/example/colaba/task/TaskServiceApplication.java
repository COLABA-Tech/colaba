package com.example.colaba.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
//@EnableFeignClients  // TODO
@EntityScan(basePackages = {"com.example.colaba.shared.entity", "com.example.colaba.task.entity"})
@EnableJpaRepositories(basePackages = "com.example.colaba.task.repository")
public class TaskServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}