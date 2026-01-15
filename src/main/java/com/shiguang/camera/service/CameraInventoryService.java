package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.CameraInventory;

public interface CameraInventoryService extends IService<CameraInventory> {

    /**
     * 增加库存
     */
    boolean increaseInventory(Integer modelId, Integer quantity);

    /**
     * 减少库存
     */
    boolean decreaseInventory(Integer modelId, Integer quantity);

    /**
     * 预订库存
     */
    boolean reserveInventory(Integer modelId, Integer quantity);

    /**
     * 取消预订
     */
    boolean cancelReservation(Integer modelId, Integer quantity);

    /**
     * 检查库存是否足够
     */
    boolean checkStockAvailable(Integer modelId, Integer requiredQuantity);

    /**
     * 获取相机型号的库存信息
     */
    CameraInventory getByModelId(Integer modelId);

    /**
     * 设备租出（减少可用数量，增加已租出数量）
     */
    boolean rentInventory(Integer modelId, Integer quantity);

    /**
     * 设备归还（增加可用数量，减少已租出数量）
     */
    boolean returnInventory(Integer modelId, Integer quantity);

    /**
     * 送修设备（减少可用数量，增加维修中数量）
     */
    boolean sendToMaintenance(Integer modelId, Integer quantity);

    /**
     * 维修完成（增加可用数量，减少维修中数量）
     */
    boolean completeMaintenance(Integer modelId, Integer quantity);

    /**
     * 标记设备损坏（减少可用数量，增加损坏数量）
     */
    boolean markDamaged(Integer modelId, Integer quantity);
}