package com.rental;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 公租房管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.rental.mapper")
public class RentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalApplication.class, args);
        System.out.println("========================================");
        System.out.println("  公租房管理系统启动成功!");
        System.out.println("  接口文档: http://localhost:8080/api/swagger-ui.html");
        System.out.println("========================================");
    }
}
