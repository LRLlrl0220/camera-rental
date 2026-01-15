package com.shiguang.camera.controller;

import com.shiguang.camera.common.Result;
import com.shiguang.camera.common.ResultCode;
import com.shiguang.camera.entity.CameraInventory;
import com.shiguang.camera.service.CameraInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/camera-inventory")
@RequiredArgsConstructor
public class CameraInventoryController {

    private final CameraInventoryService cameraInventoryService;

    /**
     * 获取所有库存列表
     * GET /api/camera-inventory
     */
    @GetMapping
    public Result<?> getInventoryList() {
        try {
            List<CameraInventory> inventoryList = cameraInventoryService.list();
            return Result.success(inventoryList);
        } catch (Exception e) {
            log.error("获取库存列表失败", e);
            return Result.error("获取库存列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据型号ID获取库存信息
     * GET /api/camera-inventory/model/{modelId}
     */
    @GetMapping("/model/{modelId}")
    public Result<?> getInventoryByModelId(@PathVariable Integer modelId) {
        try {
            CameraInventory inventory = cameraInventoryService.getByModelId(modelId);
            if (inventory == null) {
                return Result.error(ResultCode.NOT_FOUND.getCode(), "该型号的库存信息不存在");
            }
            return Result.success(inventory);
        } catch (Exception e) {
            log.error("获取库存信息失败: modelId={}", modelId, e);
            return Result.error("获取库存信息失败: " + e.getMessage());
        }
    }

    /**
     * 增加库存
     * POST /api/camera-inventory/increase
     */
    @PostMapping("/increase")
    public Result<?> increaseInventory(@RequestParam Integer modelId,
                                       @RequestParam Integer quantity) {
        try {
            boolean success = cameraInventoryService.increaseInventory(modelId, quantity);
            if (success) {
                return Result.success("增加库存成功");
            } else {
                return Result.error("增加库存失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("增加库存失败: modelId={}, quantity={}", modelId, quantity, e);
            return Result.error("增加库存失败: " + e.getMessage());
        }
    }

    /**
     * 减少库存
     * POST /api/camera-inventory/decrease
     */
    @PostMapping("/decrease")
    public Result<?> decreaseInventory(@RequestParam Integer modelId,
                                       @RequestParam Integer quantity) {
        try {
            boolean success = cameraInventoryService.decreaseInventory(modelId, quantity);
            if (success) {
                return Result.success("减少库存成功");
            } else {
                return Result.error("减少库存失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("减少库存失败: modelId={}, quantity={}", modelId, quantity, e);
            return Result.error("减少库存失败: " + e.getMessage());
        }
    }

    /**
     * 预订库存
     * POST /api/camera-inventory/reserve
     */
    @PostMapping("/reserve")
    public Result<?> reserveInventory(@RequestParam Integer modelId,
                                      @RequestParam Integer quantity) {
        try {
            boolean success = cameraInventoryService.reserveInventory(modelId, quantity);
            if (success) {
                return Result.success("预订库存成功");
            } else {
                return Result.error("预订库存失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("预订库存失败: modelId={}, quantity={}", modelId, quantity, e);
            return Result.error("预订库存失败: " + e.getMessage());
        }
    }

    /**
     * 取消预订
     * POST /api/camera-inventory/cancel-reservation
     */
    @PostMapping("/cancel-reservation")
    public Result<?> cancelReservation(@RequestParam Integer modelId,
                                       @RequestParam Integer quantity) {
        try {
            boolean success = cameraInventoryService.cancelReservation(modelId, quantity);
            if (success) {
                return Result.success("取消预订成功");
            } else {
                return Result.error("取消预订失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("取消预订失败: modelId={}, quantity={}", modelId, quantity, e);
            return Result.error("取消预订失败: " + e.getMessage());
        }
    }

    /**
     * 租出设备
     * POST /api/camera-inventory/rent
     */
    @PostMapping("/rent")
    public Result<?> rentInventory(@RequestParam Integer modelId,
                                   @RequestParam Integer quantity) {
        try {
            boolean success = cameraInventoryService.rentInventory(modelId, quantity);
            if (success) {
                return Result.success("租出设备成功");
            } else {
                return Result.error("租出设备失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("租出设备失败: modelId={}, quantity={}", modelId, quantity, e);
            return Result.error("租出设备失败: " + e.getMessage());
        }
    }

    /**
     * 归还设备
     * POST /api/camera-inventory/return
     */
    @PostMapping("/return")
    public Result<?> returnInventory(@RequestParam Integer modelId,
                                     @RequestParam Integer quantity) {
        try {
            boolean success = cameraInventoryService.returnInventory(modelId, quantity);
            if (success) {
                return Result.success("归还设备成功");
            } else {
                return Result.error("归还设备失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("归还设备失败: modelId={}, quantity={}", modelId, quantity, e);
            return Result.error("归还设备失败: " + e.getMessage());
        }
    }

    /**
     * 检查库存是否足够
     * GET /api/camera-inventory/check-stock
     */
    @GetMapping("/check-stock")
    public Result<?> checkStockAvailable(@RequestParam Integer modelId,
                                         @RequestParam Integer requiredQuantity) {
        try {
            boolean available = cameraInventoryService.checkStockAvailable(modelId, requiredQuantity);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("available", available);
            result.put("message", available ? "库存足够" : "库存不足");
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查库存失败: modelId={}, requiredQuantity={}", modelId, requiredQuantity, e);
            return Result.error("检查库存失败: " + e.getMessage());
        }
    }

    /**
     * 获取库存统计信息
     * GET /api/camera-inventory/stats
     */
    @GetMapping("/stats")
    public Result<?> getInventoryStats() {
        try {
            // 这里可以添加统计逻辑，例如总库存、可用库存、预订库存等
            List<CameraInventory> inventoryList = cameraInventoryService.list();
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalModels", inventoryList.size());
            // 可以添加更多统计信息
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取库存统计信息失败", e);
            return Result.error("获取库存统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新库存信息
     * PUT /api/camera-inventory/{id}
     */
    @PutMapping("/{id}")
    public Result<?> updateInventory(@PathVariable Integer id,
                                     @RequestBody CameraInventory inventory) {
        try {
            inventory.setId(id);
            boolean success = cameraInventoryService.updateById(inventory);
            if (success) {
                return Result.success("更新库存信息成功");
            } else {
                return Result.error("更新库存信息失败");
            }
        } catch (Exception e) {
            log.error("更新库存信息失败: id={}", id, e);
            return Result.error("更新库存信息失败: " + e.getMessage());
        }
    }

    /**
     * 初始化库存（为没有库存记录的型号创建库存记录）
     * POST /api/camera-inventory/init/{modelId}
     */
    @PostMapping("/init/{modelId}")
    public Result<?> initInventory(@PathVariable Integer modelId,
                                   @RequestBody(required = false) CameraInventory inventory) {
        try {
            if (inventory == null) {
                inventory = new CameraInventory();
            }
            inventory.setModelId(modelId);

            // 设置默认值
            if (inventory.getTotalQuantity() == null) {
                // 获取该型号的设备实例数量作为初始库存
                int instanceCount = 0; // 这里需要查询设备实例数量，简化处理
                inventory.setTotalQuantity(instanceCount);
                inventory.setAvailableQuantity(instanceCount);
            }

            if (inventory.getLowStockThreshold() == null) {
                inventory.setLowStockThreshold(2);
            }

            if (inventory.getReorderPoint() == null) {
                inventory.setReorderPoint(1);
            }

            boolean success = cameraInventoryService.save(inventory);
            if (success) {
                return Result.success("初始化库存成功", inventory);
            } else {
                return Result.error("初始化库存失败");
            }
        } catch (Exception e) {
            log.error("初始化库存失败: modelId={}", modelId, e);
            return Result.error("初始化库存失败: " + e.getMessage());
        }
    }
}