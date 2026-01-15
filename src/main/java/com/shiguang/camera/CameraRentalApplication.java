package com.shiguang.camera;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.shiguang.camera.mapper")
@EnableScheduling  // 启用定时任务（用于清理过期验证码）
//Spring Boot启动类
public class CameraRentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CameraRentalApplication.class, args);
        System.out.println("==================================");
        System.out.println("拾光相机租赁系统后端启动成功!");
        System.out.println("后端地址: http://localhost:8081");
        System.out.println("数据库: camera_rental (MySQL 8.0.17)");
        System.out.println("==================================");
    }
}