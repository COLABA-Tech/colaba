package com.example.colaba.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.project",
        "com.example.colaba.shared.common",
        "com.example.colaba.shared.webflux"
})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.example.colaba.project.repository")
public class ProjectServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }
}