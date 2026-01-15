package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 获取可用设备列表（带库存信息）
     */
    @Select("SELECT d.*, di.available_quantity, di.total_quantity " +
            "FROM devices d " +
            "LEFT JOIN device_inventory di ON d.id = di.device_id " +
            "WHERE d.status = 'available' " +
            "AND (di.available_quantity > 0 OR di.available_quantity IS NULL) " +
            "ORDER BY d.created_at DESC")
    List<Device> selectAvailableDevicesWithInventory();

    /**
     * 根据分类获取设备
     */
    @Select("SELECT d.*, di.available_quantity, di.total_quantity " +
            "FROM devices d " +
            "LEFT JOIN device_inventory di ON d.id = di.device_id " +
            "WHERE d.category = #{category} " +
            "AND d.status = 'available' " +
            "AND (di.available_quantity > 0 OR di.available_quantity IS NULL) " +
            "ORDER BY d.daily_price ASC")
    List<Device> selectDevicesByCategory(String category);

    /**
     * 搜索设备
     */
    @Select("<script>" +
            "SELECT d.*, di.available_quantity, di.total_quantity " +
            "FROM devices d " +
            "LEFT JOIN device_inventory di ON d.id = di.device_id " +
            "WHERE d.status = 'available' " +
            "AND (di.available_quantity > 0 OR di.available_quantity IS NULL) " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (d.name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR d.description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR d.brand LIKE CONCAT('%', #{keyword}, '%') " +
            "OR d.model LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY d.created_at DESC" +
            "</script>")
    List<Device> searchDevices(String keyword);

    /**
     * 获取设备详情（带库存）
     */
    @Select("SELECT d.*, di.* " +
            "FROM devices d " +
            "LEFT JOIN device_inventory di ON d.id = di.device_id " +
            "WHERE d.id = #{id}")
    Device selectDeviceDetailById(Integer id);  // 改为 Integer

    /**
     * 更新设备状态
     */
    @Select("UPDATE devices SET status = #{status}, updated_at = NOW() WHERE id = #{deviceId}")
    int updateDeviceStatus(Integer deviceId, String status);  // deviceId 改为 Integer

    /**
     * 获取设备价格
     */
    @Select("SELECT daily_price FROM devices WHERE id = #{deviceId}")
    BigDecimal getDevicePrice(Integer deviceId);  // 改为 Integer
}