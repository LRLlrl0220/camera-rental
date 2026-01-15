package com.shiguang.camera.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.CameraModel;
import java.util.List;
import java.util.Map;

public interface CameraModelService extends IService<CameraModel> {

    /**
     * 获取所有启用的相机型号
     */
    List<CameraModel> getAllEnabledModels();

    /**
     * 根据品牌ID获取相机型号
     */
    List<CameraModel> getModelsByBrandId(Integer brandId);

    /**
     * 搜索相机型号
     */
    List<CameraModel> searchModels(String keyword);

    /**
     * 分页查询相机型号（带搜索条件）
     */
    IPage<CameraModel> getModelPage(Integer page, Integer size,
                                    Integer brandId, String keyword, Integer status);

    /**
     * 添加相机型号
     */
    boolean addCameraModel(CameraModel cameraModel);

    /**
     * 更新相机型号
     */
    boolean updateCameraModel(CameraModel cameraModel);

    /**
     * 启用/停用相机型号
     */
    boolean toggleModelStatus(Integer id, Integer status);

    /**
     * 获取相机型号详情（包含品牌信息）
     */
    CameraModel getModelDetail(Integer id);

    /**
     * 获取相机型号统计信息
     */
    Map<String, Object> getModelStats();
}