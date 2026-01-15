package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.DeviceInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DeviceInventoryMapper extends BaseMapper<DeviceInventory> {

    @Update("UPDATE device_inventory SET " +
            "available_quantity = available_quantity - #{quantity}, " +
            "reserved_quantity = reserved_quantity + #{quantity}, " +
            "last_updated = NOW() " +
            "WHERE device_id = #{deviceId} AND available_quantity >= #{quantity}")
    int reserveInventory(@Param("deviceId") Integer deviceId, @Param("quantity") Integer quantity);  // 改为 Integer

    @Update("UPDATE device_inventory SET " +
            "available_quantity = available_quantity + #{quantity}, " +
            "reserved_quantity = reserved_quantity - #{quantity}, " +
            "last_updated = NOW() " +
            "WHERE device_id = #{deviceId} AND reserved_quantity >= #{quantity}")
    int cancelReserve(@Param("deviceId") Integer deviceId, @Param("quantity") Integer quantity);  // 改为 Integer

    @Update("UPDATE device_inventory SET " +
            "reserved_quantity = reserved_quantity - #{quantity}, " +
            "rented_quantity = rented_quantity + #{quantity}, " +
            "last_updated = NOW() " +
            "WHERE device_id = #{deviceId} AND reserved_quantity >= #{quantity}")
    int confirmReservation(@Param("deviceId") Integer deviceId, @Param("quantity") Integer quantity);  // 改为 Integer

    @Update("UPDATE device_inventory SET " +
            "rented_quantity = rented_quantity - #{quantity}, " +
            "available_quantity = available_quantity + #{quantity}, " +
            "last_updated = NOW() " +
            "WHERE device_id = #{deviceId} AND rented_quantity >= #{quantity}")
    int completeReservation(@Param("deviceId") Integer deviceId, @Param("quantity") Integer quantity);  // 改为 Integer

    @Select("SELECT available_quantity FROM device_inventory WHERE device_id = #{deviceId}")
    Integer getAvailableQuantity(@Param("deviceId") Integer deviceId);  // 改为 Integer
}