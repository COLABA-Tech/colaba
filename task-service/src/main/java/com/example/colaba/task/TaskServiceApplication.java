package com.example.colaba.task;

import com.example.colaba.shared.common.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.task",
        "com.example.colaba.shared.common",
        "com.example.colaba.shared.webmvc"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.colaba.shared.webmvc.client")
@Import(FeignConfig.class)
@EnableJpaRepositories(basePackages = "com.example.colaba.task.repository")
public class TaskServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}