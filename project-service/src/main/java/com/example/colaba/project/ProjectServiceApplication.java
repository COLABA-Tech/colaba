package com.example.colaba.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
//@EnableFeignClients  // TODO
@EntityScan(basePackages = {"com.example.colaba.shared.entity", "com.example.colaba.project.entity"})
@EnableJpaRepositories(basePackages = "com.example.colaba.project.repository")
public class ProjectServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }
}