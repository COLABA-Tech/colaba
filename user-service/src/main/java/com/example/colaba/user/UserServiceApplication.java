package com.example.colaba.user;

import com.example.colaba.shared.common.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.user",
        "com.example.colaba.shared.common",
        "com.example.colaba.shared.webflux"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.colaba.user.client")
@Import(FeignConfig.class)
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}