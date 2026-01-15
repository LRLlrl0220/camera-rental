package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.Device;
import com.shiguang.camera.mapper.DeviceMapper;
import com.shiguang.camera.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    private final DeviceMapper deviceMapper;

    @Override
    public List<Device> getAvailableDevices() {
        return deviceMapper.selectAvailableDevicesWithInventory();
    }

    @Override
    public List<Device> getDevicesByCategory(String category) {
        return deviceMapper.selectDevicesByCategory(category);
    }

    @Override
    public List<Device> searchDevices(String keyword) {
        return deviceMapper.searchDevices(keyword);
    }

    /**
     * 获取设备详情（带库存信息）
     */
    public Device getDeviceDetailById(Integer id) {
        return deviceMapper.selectDeviceDetailById(id);
    }

    /**
     * 更新设备状态
     */
    public boolean updateDeviceStatus(Integer deviceId, String status) {
        int rows = deviceMapper.updateDeviceStatus(deviceId, status);
        return rows > 0;
    }

    /**
     * 获取设备价格
     */
    public BigDecimal getDevicePrice(Integer deviceId) {
        return deviceMapper.getDevicePrice(deviceId);
    }
}