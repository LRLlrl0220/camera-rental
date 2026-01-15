package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.CameraInventory;
import com.shiguang.camera.mapper.CameraInventoryMapper;
import com.shiguang.camera.service.CameraInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CameraInventoryServiceImpl extends ServiceImpl<CameraInventoryMapper, CameraInventory>
        implements CameraInventoryService {

    @Override
    @Transactional
    public boolean increaseInventory(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("增加数量必须大于0");
        }

        CameraInventory inventory = getByModelId(modelId);
        if (inventory == null) {
            // 如果还没有库存记录，创建一条
            inventory = new CameraInventory();
            inventory.setModelId(modelId);
            inventory.setTotalQuantity(quantity);
            inventory.setAvailableQuantity(quantity);
            inventory.setReservedQuantity(0);
            inventory.setRentedQuantity(0);
            inventory.setMaintenanceQuantity(0);
            inventory.setDamagedQuantity(0);
            inventory.setLowStockThreshold(2);
            inventory.setReorderPoint(1);
            return save(inventory);
        }

        int affected = baseMapper.increaseInventory(modelId, quantity);
        return affected > 0;
    }

    @Override
    @Transactional
    public boolean decreaseInventory(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("减少数量必须大于0");
        }

        int affected = baseMapper.decreaseInventory(modelId, quantity);
        return affected > 0;
    }

    @Override
    @Transactional
    public boolean reserveInventory(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("预订数量必须大于0");
        }

        int affected = baseMapper.reserveInventory(modelId, quantity);
        return affected > 0;
    }

    @Override
    @Transactional
    public boolean cancelReservation(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("取消预订数量必须大于0");
        }

        int affected = baseMapper.cancelReservation(modelId, quantity);
        return affected > 0;
    }

    @Override
    public boolean checkStockAvailable(Integer modelId, Integer requiredQuantity) {
        if (requiredQuantity <= 0) {
            return true;
        }

        Boolean result = baseMapper.checkStockAvailable(modelId, requiredQuantity);
        return result != null && result;
    }

    @Override
    public CameraInventory getByModelId(Integer modelId) {
        QueryWrapper<CameraInventory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("model_id", modelId);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean rentInventory(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("租出数量必须大于0");
        }

        int affected = baseMapper.rentInventory(modelId, quantity);
        return affected > 0;
    }

    @Override
    @Transactional
    public boolean returnInventory(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("归还数量必须大于0");
        }

        int affected = baseMapper.returnInventory(modelId, quantity);
        return affected > 0;
    }

    @Override
    @Transactional
    public boolean sendToMaintenance(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("送修数量必须大于0");
        }

        // 先检查库存是否足够
        CameraInventory inventory = getByModelId(modelId);
        if (inventory == null || inventory.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("可用库存不足");
        }

        // 更新库存：减少可用，增加维修中
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setMaintenanceQuantity(inventory.getMaintenanceQuantity() + quantity);
        return updateById(inventory);
    }

    @Override
    @Transactional
    public boolean completeMaintenance(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("维修完成数量必须大于0");
        }

        CameraInventory inventory = getByModelId(modelId);
        if (inventory == null || inventory.getMaintenanceQuantity() < quantity) {
            throw new IllegalArgumentException("维修中数量不足");
        }

        // 更新库存：增加可用，减少维修中
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setMaintenanceQuantity(inventory.getMaintenanceQuantity() - quantity);
        return updateById(inventory);
    }

    @Override
    @Transactional
    public boolean markDamaged(Integer modelId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("损坏数量必须大于0");
        }

        CameraInventory inventory = getByModelId(modelId);
        if (inventory == null || inventory.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("可用库存不足");
        }

        // 更新库存：减少可用，增加损坏
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setDamagedQuantity(inventory.getDamagedQuantity() + quantity);
        return updateById(inventory);
    }
}