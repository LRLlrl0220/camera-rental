package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.Device;
import java.util.List;

public interface DeviceService extends IService<Device> {

    /**
     * 获取所有可用设备
     */
    List<Device> getAvailableDevices();

    /**
     * 根据分类获取设备
     */
    List<Device> getDevicesByCategory(String category);

    /**
     * 搜索设备
     */
    List<Device> searchDevices(String keyword);
}