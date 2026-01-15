package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.SystemConfig;

import java.util.Map;

public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 获取所有配置
     */
    Map<String, String> getAllConfigs();

    /**
     * 更新配置
     */
    boolean updateConfig(String key, String value);

    /**
     * 获取配置值
     */
    String getConfigValue(String key, String defaultValue);
}