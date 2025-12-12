package com.example.colaba.user;

import com.example.colaba.shared.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.user",
        "com.example.colaba.shared.mapper",
        "com.example.colaba.shared.exception",
        "com.example.colaba.shared.circuit"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.colaba.shared.client")
@Import(FeignConfig.class)
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}