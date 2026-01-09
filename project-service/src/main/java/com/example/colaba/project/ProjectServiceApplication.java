package com.example.colaba.project;

import com.example.colaba.shared.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.project",
        "com.example.colaba.shared.exception",
        "com.example.colaba.shared.circuit"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.colaba.project.client")
@Import(FeignConfig.class)
@EntityScan(basePackages = {"com.example.colaba.shared.entity", "com.example.colaba.project.entity"})
@EnableJpaRepositories(basePackages = "com.example.colaba.project.repository")
public class ProjectServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }
}