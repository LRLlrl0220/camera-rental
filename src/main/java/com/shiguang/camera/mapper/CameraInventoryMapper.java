package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.CameraInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CameraInventoryMapper extends BaseMapper<CameraInventory> {

    /**
     * 增加库存数量
     */
    @Update("UPDATE camera_inventory SET total_quantity = total_quantity + #{quantity}, " +
            "available_quantity = available_quantity + #{quantity} WHERE model_id = #{modelId}")
    int increaseInventory(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 减少库存数量
     */
    @Update("UPDATE camera_inventory SET total_quantity = total_quantity - #{quantity}, " +
            "available_quantity = available_quantity - #{quantity} " +
            "WHERE model_id = #{modelId} AND available_quantity >= #{quantity}")
    int decreaseInventory(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 预订设备（减少可用数量，增加已预订数量）
     */
    @Update("UPDATE camera_inventory SET available_quantity = available_quantity - #{quantity}, " +
            "reserved_quantity = reserved_quantity + #{quantity} " +
            "WHERE model_id = #{modelId} AND available_quantity >= #{quantity}")
    int reserveInventory(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 取消预订（增加可用数量，减少已预订数量）
     */
    @Update("UPDATE camera_inventory SET available_quantity = available_quantity + #{quantity}, " +
            "reserved_quantity = reserved_quantity - #{quantity} " +
            "WHERE model_id = #{modelId} AND reserved_quantity >= #{quantity}")
    int cancelReservation(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 设备租出（减少可用数量，增加已租出数量）
     */
    @Update("UPDATE camera_inventory SET available_quantity = available_quantity - #{quantity}, " +
            "rented_quantity = rented_quantity + #{quantity} " +
            "WHERE model_id = #{modelId} AND available_quantity >= #{quantity}")
    int rentInventory(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 设备归还（增加可用数量，减少已租出数量）
     */
    @Update("UPDATE camera_inventory SET available_quantity = available_quantity + #{quantity}, " +
            "rented_quantity = rented_quantity - #{quantity} " +
            "WHERE model_id = #{modelId} AND rented_quantity >= #{quantity}")
    int returnInventory(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 送修设备（减少可用数量，增加维修中数量）
     */
    @Update("UPDATE camera_inventory SET available_quantity = available_quantity - #{quantity}, " +
            "maintenance_quantity = maintenance_quantity + #{quantity} " +
            "WHERE model_id = #{modelId} AND available_quantity >= #{quantity}")
    int sendToMaintenance(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 维修完成（增加可用数量，减少维修中数量）
     */
    @Update("UPDATE camera_inventory SET available_quantity = available_quantity + #{quantity}, " +
            "maintenance_quantity = maintenance_quantity - #{quantity} " +
            "WHERE model_id = #{modelId} AND maintenance_quantity >= #{quantity}")
    int completeMaintenance(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 标记设备损坏（减少可用数量，增加损坏数量）
     */
    @Update("UPDATE camera_inventory SET available_quantity = available_quantity - #{quantity}, " +
            "damaged_quantity = damaged_quantity + #{quantity} " +
            "WHERE model_id = #{modelId} AND available_quantity >= #{quantity}")
    int markDamaged(@Param("modelId") Integer modelId, @Param("quantity") Integer quantity);

    /**
     * 检查库存是否足够
     */
    @Select("SELECT available_quantity >= #{requiredQuantity} FROM camera_inventory WHERE model_id = #{modelId}")
    Boolean checkStockAvailable(@Param("modelId") Integer modelId, @Param("requiredQuantity") Integer requiredQuantity);

    /**
     * 根据型号ID获取库存信息
     */
    @Select("SELECT * FROM camera_inventory WHERE model_id = #{modelId}")
    CameraInventory selectByModelId(@Param("modelId") Integer modelId);
}