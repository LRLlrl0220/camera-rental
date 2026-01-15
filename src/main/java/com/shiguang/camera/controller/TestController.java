package com.shiguang.camera.controller;

import com.shiguang.camera.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
//测试控制器
public class TestController {
    
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("拾光相机租赁系统后端运行正常！");
    }
    
    @GetMapping("/status")
    public Result<?> status() {
        return Result.success("系统状态正常", 
            new SystemStatus("1.0.0", "运行中", "JDK 1.8", "MySQL 8.0.17"));
    }
    
    // 内部类用于返回系统状态信息
    private static class SystemStatus {
        private String version;
        private String status;
        private String javaVersion;
        private String database;
        
        public SystemStatus(String version, String status, String javaVersion, String database) {
            this.version = version;
            this.status = status;
            this.javaVersion = javaVersion;
            this.database = database;
        }
        
        // getter方法
        public String getVersion() { return version; }
        public String getStatus() { return status; }
        public String getJavaVersion() { return javaVersion; }
        public String getDatabase() { return database; }
    }
}