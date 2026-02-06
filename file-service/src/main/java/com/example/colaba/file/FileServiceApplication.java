package com.example.colaba.file;

import com.example.colaba.shared.webmvc.feign.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.example.colaba.file",
        "com.example.colaba.shared.common",
        "com.example.colaba.shared.webmvc"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.colaba.shared.webmvc.client")
@Import(FeignConfig.class)
public class FileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}