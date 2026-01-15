package com.shiguang.camera.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shiguang.camera.common.Result;
import com.shiguang.camera.common.ResultCode;
import com.shiguang.camera.entity.CameraInstance;
import com.shiguang.camera.service.CameraInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/camera-instances")
@RequiredArgsConstructor
public class CameraInstanceController {

    private final CameraInstanceService cameraInstanceService;

    /**
     * 获取设备实例列表（分页）
     * GET /api/camera-instances?page=1&size=10&modelId=1&serialNumber=ABC&status=0&keyword=相机
     */
    @GetMapping
    public Result<?> getCameraInstanceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer modelId,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        try {
            IPage<CameraInstance> pageResult = cameraInstanceService.getInstancePage(
                    page, size, modelId, serialNumber, status, keyword);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取设备实例列表失败", e);
            return Result.error("获取设备实例列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据相机型号ID获取设备实例列表
     * GET /api/camera-instances/model/{modelId}
     */
    @GetMapping("/model/{modelId}")
    public Result<?> getInstancesByModelId(@PathVariable Integer modelId) {
        try {
            List<CameraInstance> instances = cameraInstanceService.getInstancesByModelId(modelId);
            return Result.success(instances);
        } catch (Exception e) {
            log.error("获取型号设备实例失败: modelId={}", modelId, e);
            return Result.error("获取型号设备实例失败: " + e.getMessage());
        }
    }

    /**
     * 根据相机型号ID获取可用设备实例
     * GET /api/camera-instances/model/{modelId}/available
     */
    @GetMapping("/model/{modelId}/available")
    public Result<?> getAvailableInstancesByModelId(@PathVariable Integer modelId) {
        try {
            List<CameraInstance> instances = cameraInstanceService.getAvailableInstancesByModelId(modelId);
            return Result.success(instances);
        } catch (Exception e) {
            log.error("获取可用设备实例失败: modelId={}", modelId, e);
            return Result.error("获取可用设备实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备实例详情
     * GET /api/camera-instances/{id}
     */
    @GetMapping("/{id}")
    public Result<?> getCameraInstanceDetail(@PathVariable Integer id) {
        try {
            CameraInstance instance = cameraInstanceService.getInstanceDetail(id);
            if (instance == null) {
                return Result.error(ResultCode.NOT_FOUND.getCode(), "设备实例不存在");
            }
            return Result.success(instance);
        } catch (Exception e) {
            log.error("获取设备实例详情失败: id={}", id, e);
            return Result.error("获取设备实例详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建设备实例
     * POST /api/camera-instances
     */
    @PostMapping
    public Result<?> createCameraInstance(@RequestBody CameraInstance cameraInstance) {
        try {
            boolean success = cameraInstanceService.createCameraInstance(cameraInstance);
            if (success) {
                return Result.success("创建设备实例成功", cameraInstance);
            } else {
                return Result.error("创建设备实例失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建设备实例失败", e);
            return Result.error("创建设备实例失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备实例
     * PUT /api/camera-instances/{id}
     */
    @PutMapping("/{id}")
    public Result<?> updateCameraInstance(@PathVariable Integer id,
                                          @RequestBody CameraInstance cameraInstance) {
        try {
            cameraInstance.setId(id);
            boolean success = cameraInstanceService.updateCameraInstance(cameraInstance);
            if (success) {
                return Result.success("更新设备实例成功");
            } else {
                return Result.error("更新设备实例失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新设备实例失败: id={}", id, e);
            return Result.error("更新设备实例失败: " + e.getMessage());
        }
    }

    /**
     * 删除设备实例
     * DELETE /api/camera-instances/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteCameraInstance(@PathVariable Integer id) {
        try {
            boolean success = cameraInstanceService.deleteCameraInstance(id);
            if (success) {
                return Result.success("删除设备实例成功");
            } else {
                return Result.error("删除设备实例失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除设备实例失败: id={}", id, e);
            return Result.error("删除设备实例失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备状态
     * PATCH /api/camera-instances/{id}/status
     */
    @PatchMapping("/{id}/status")
    public Result<?> updateInstanceStatus(@PathVariable Integer id,
                                          @RequestParam Integer status) {
        try {
            boolean success = cameraInstanceService.updateInstanceStatus(id, status);
            if (success) {
                String message = getStatusMessage(status);
                return Result.success(message);
            } else {
                return Result.error("更新设备状态失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新设备状态失败: id={}, status={}", id, status, e);
            return Result.error("更新设备状态失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备状况
     * PATCH /api/camera-instances/{id}/condition
     */
    @PatchMapping("/{id}/condition")
    public Result<?> updateInstanceCondition(@PathVariable Integer id,
                                             @RequestParam Integer condition) {
        try {
            boolean success = cameraInstanceService.updateInstanceCondition(id, condition);
            if (success) {
                String message = getConditionMessage(condition);
                return Result.success(message);
            } else {
                return Result.error("更新设备状况失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新设备状况失败: id={}, condition={}", id, condition, e);
            return Result.error("更新设备状况失败: " + e.getMessage());
        }
    }

    /**
     * 根据序列号查询设备实例
     * GET /api/camera-instances/serial/{serialNumber}
     */
    @GetMapping("/serial/{serialNumber}")
    public Result<?> getInstanceBySerialNumber(@PathVariable String serialNumber) {
        try {
            CameraInstance instance = cameraInstanceService.getInstanceBySerialNumber(serialNumber);
            if (instance == null) {
                return Result.error(ResultCode.NOT_FOUND.getCode(), "设备实例不存在");
            }
            return Result.success(instance);
        } catch (Exception e) {
            log.error("查询设备实例失败: serialNumber={}", serialNumber, e);
            return Result.error("查询设备实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备实例统计信息
     * GET /api/camera-instances/stats?modelId=1
     */
    @GetMapping("/stats")
    public Result<?> getCameraInstanceStats(@RequestParam(required = false) Integer modelId) {
        try {
            Map<String, Object> stats = cameraInstanceService.getInstanceStats(modelId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取设备实例统计信息失败", e);
            return Result.error("获取设备实例统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建设备实例
     * POST /api/camera-instances/batch
     */
    @PostMapping("/batch")
    public Result<?> batchCreateInstances(@RequestParam Integer modelId,
                                          @RequestBody List<CameraInstance> instances) {
        try {
            boolean success = cameraInstanceService.batchCreateInstances(modelId, instances);
            if (success) {
                return Result.success("批量创建设备实例成功，共创建" + instances.size() + "个");
            } else {
                return Result.error("批量创建设备实例失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量创建设备实例失败: modelId={}", modelId, e);
            return Result.error("批量创建设备实例失败: " + e.getMessage());
        }
    }

    /**
     * 检查序列号是否唯一
     * GET /api/camera-instances/check-serial
     */
    @GetMapping("/check-serial")
    public Result<?> checkSerialNumberUnique(@RequestParam String serialNumber,
                                             @RequestParam(required = false) Integer excludeId) {
        try {
            boolean isUnique = cameraInstanceService.isSerialNumberUnique(serialNumber, excludeId);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("isUnique", isUnique);
            result.put("message", isUnique ? "序列号可用" : "序列号已存在");
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查序列号失败: serialNumber={}", serialNumber, e);
            return Result.error("检查序列号失败: " + e.getMessage());
        }
    }

    /**
     * 获取状态对应的中文消息
     */
    private String getStatusMessage(Integer status) {
        switch (status) {
            case 0: return "设备已设为可用状态";
            case 1: return "设备已设为已预订状态";
            case 2: return "设备已设为租赁中状态";
            case 3: return "设备已设为维修中状态";
            case 4: return "设备已设为已下架状态";
            default: return "更新设备状态成功";
        }
    }

    /**
     * 获取状况对应的中文消息
     */
    private String getConditionMessage(Integer condition) {
        switch (condition) {
            case 0: return "设备状况已设为优";
            case 1: return "设备状况已设为良";
            case 2: return "设备状况已设为中";
            case 3: return "设备状况已设为需维修";
            default: return "更新设备状况成功";
        }
    }
}