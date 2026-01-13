package com.example.colaba.task;

import com.example.colaba.shared.common.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.task",
        "com.example.colaba.shared.common.exception",
        "com.example.colaba.shared.common.security",
        "com.example.colaba.shared.webmvc.filter"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.colaba.task.client")
@Import(FeignConfig.class)
@EntityScan(basePackages = {"com.example.colaba.task.entity"})
@EnableJpaRepositories(basePackages = "com.example.colaba.task.repository")
public class TaskServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}