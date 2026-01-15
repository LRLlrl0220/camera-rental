package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.SystemConfig;
import com.shiguang.camera.mapper.SystemConfigMapper;
import com.shiguang.camera.service.RefundCalculator;
import com.shiguang.camera.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final RefundCalculator refundCalculator;

    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = this.list();
        Map<String, String> result = new HashMap<>();

        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }

        return result;
    }

    @Override
    public boolean updateConfig(String key, String value) {
        SystemConfig config = this.getOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key)
        );

        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            return this.save(config);
        } else {
            config.setConfigValue(value);
            boolean updated = this.updateById(config);

            // 更新成功后清除退款计算器的缓存
            if (updated) {
                refundCalculator.clearConfigCache();
                log.info("更新系统配置 {} = {}，已清除缓存", key, value);
            }

            return updated;
        }
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        SystemConfig config = this.getOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key)
        );

        return config != null ? config.getConfigValue() : defaultValue;
    }
}