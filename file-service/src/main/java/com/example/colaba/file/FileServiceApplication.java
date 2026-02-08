package com.example.colaba.file;

import com.example.colaba.shared.webmvc.infrastructure.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.file",
        "com.example.colaba.shared.common",
        "com.example.colaba.shared.webmvc"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.colaba.shared.webmvc.infrastructure.client")
@Import(FeignConfig.class)
@EnableJpaRepositories(basePackages = "com.example.colaba.file.repository")
public class FileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}