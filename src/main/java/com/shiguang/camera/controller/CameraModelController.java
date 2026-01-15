package com.shiguang.camera.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shiguang.camera.common.Result;
import com.shiguang.camera.common.ResultCode;
import com.shiguang.camera.entity.CameraModel;
import com.shiguang.camera.service.CameraModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/camera-models")
@RequiredArgsConstructor
public class CameraModelController {

    private final CameraModelService cameraModelService;

    /**
     * 获取相机型号列表（分页）
     * GET /api/camera-models?page=1&size=10&brandId=1&keyword=相机&status=1
     */
    @GetMapping
    public Result<?> getCameraModelList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        try {
            IPage<CameraModel> pageResult = cameraModelService.getModelPage(
                    page, size, brandId, keyword, status);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取相机型号列表失败", e);
            return Result.error("获取相机型号列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的相机型号（用于下拉选择）
     * GET /api/camera-models/enabled
     */
    @GetMapping("/enabled")
    public Result<?> getEnabledCameraModels() {
        try {
            List<CameraModel> models = cameraModelService.getAllEnabledModels();
            return Result.success(models);
        } catch (Exception e) {
            log.error("获取启用的相机型号失败", e);
            return Result.error("获取启用的相机型号失败: " + e.getMessage());
        }
    }

    /**
     * 根据品牌ID获取相机型号
     * GET /api/camera-models/brand/{brandId}
     */
    @GetMapping("/brand/{brandId}")
    public Result<?> getModelsByBrandId(@PathVariable Integer brandId) {
        try {
            List<CameraModel> models = cameraModelService.getModelsByBrandId(brandId);
            return Result.success(models);
        } catch (Exception e) {
            log.error("获取品牌相机型号失败: brandId={}", brandId, e);
            return Result.error("获取品牌相机型号失败: " + e.getMessage());
        }
    }

    /**
     * 搜索相机型号
     * GET /api/camera-models/search?keyword=相机
     */
    @GetMapping("/search")
    public Result<?> searchCameraModels(@RequestParam String keyword) {
        try {
            List<CameraModel> models = cameraModelService.searchModels(keyword);
            return Result.success(models);
        } catch (Exception e) {
            log.error("搜索相机型号失败: keyword={}", keyword, e);
            return Result.error("搜索相机型号失败: " + e.getMessage());
        }
    }

    /**
     * 获取相机型号详情
     * GET /api/camera-models/{id}
     */
    @GetMapping("/{id}")
    public Result<?> getCameraModelDetail(@PathVariable Integer id) {
        try {
            CameraModel cameraModel = cameraModelService.getModelDetail(id);
            if (cameraModel == null) {
                return Result.error(ResultCode.NOT_FOUND.getCode(), "相机型号不存在");
            }
            return Result.success(cameraModel);
        } catch (Exception e) {
            log.error("获取相机型号详情失败: id={}", id, e);
            return Result.error("获取相机型号详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加相机型号
     * POST /api/camera-models
     */
    @PostMapping
    public Result<?> addCameraModel(@RequestBody CameraModel cameraModel) {
        try {
            boolean success = cameraModelService.addCameraModel(cameraModel);
            if (success) {
                return Result.success("添加相机型号成功", cameraModel);
            } else {
                return Result.error("添加相机型号失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("添加相机型号失败", e);
            return Result.error("添加相机型号失败: " + e.getMessage());
        }
    }

    /**
     * 更新相机型号
     * PUT /api/camera-models/{id}
     */
    @PutMapping("/{id}")
    public Result<?> updateCameraModel(@PathVariable Integer id,
                                       @RequestBody CameraModel cameraModel) {
        try {
            cameraModel.setId(id);
            boolean success = cameraModelService.updateCameraModel(cameraModel);
            if (success) {
                return Result.success("更新相机型号成功");
            } else {
                return Result.error("更新相机型号失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新相机型号失败: id={}", id, e);
            return Result.error("更新相机型号失败: " + e.getMessage());
        }
    }

    /**
     * 启用/停用相机型号
     * PATCH /api/camera-models/{id}/status
     */
    @PatchMapping("/{id}/status")
    public Result<?> toggleCameraModelStatus(@PathVariable Integer id,
                                             @RequestParam Integer status) {
        try {
            boolean success = cameraModelService.toggleModelStatus(id, status);
            if (success) {
                String message = status == 1 ? "启用相机型号成功" : "停用相机型号成功";
                return Result.success(message);
            } else {
                return Result.error("更新相机型号状态失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新相机型号状态失败: id={}, status={}", id, status, e);
            return Result.error("更新相机型号状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除相机型号
     * DELETE /api/camera-models/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteCameraModel(@PathVariable Integer id) {
        try {
            boolean success = cameraModelService.removeById(id);
            if (success) {
                return Result.success("删除相机型号成功");
            } else {
                return Result.error("删除相机型号失败");
            }
        } catch (Exception e) {
            log.error("删除相机型号失败: id={}", id, e);
            return Result.error("删除相机型号失败: " + e.getMessage());
        }
    }

    /**
     * 获取相机型号统计信息
     * GET /api/camera-models/stats
     */
    @GetMapping("/stats")
    public Result<?> getCameraModelStats() {
        try {
            Map<String, Object> stats = cameraModelService.getModelStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取相机型号统计信息失败", e);
            return Result.error("获取相机型号统计信息失败: " + e.getMessage());
        }
    }
}