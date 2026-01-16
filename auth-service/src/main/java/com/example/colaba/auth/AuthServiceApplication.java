package com.example.colaba.auth;

import com.example.colaba.shared.common.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.auth",
        "com.example.colaba.shared.common",
        "com.example.colaba.shared.webmvc"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.colaba.shared.webmvc.client")
@Import(FeignConfig.class)
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}