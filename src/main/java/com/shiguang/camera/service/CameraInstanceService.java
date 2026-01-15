package com.shiguang.camera.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.CameraInstance;
import java.util.List;
import java.util.Map;

public interface CameraInstanceService extends IService<CameraInstance> {

    /**
     * 根据相机型号ID获取设备实例列表
     */
    List<CameraInstance> getInstancesByModelId(Integer modelId);

    /**
     * 根据相机型号ID获取可用设备实例
     */
    List<CameraInstance> getAvailableInstancesByModelId(Integer modelId);

    /**
     * 分页查询设备实例（带搜索条件）
     */
    IPage<CameraInstance> getInstancePage(Integer page, Integer size,
                                          Integer modelId, String serialNumber,
                                          Integer status, String keyword);

    /**
     * 创建设备实例
     */
    boolean createCameraInstance(CameraInstance cameraInstance);

    /**
     * 更新设备实例
     */
    boolean updateCameraInstance(CameraInstance cameraInstance);

    /**
     * 删除设备实例
     */
    boolean deleteCameraInstance(Integer id);

    /**
     * 更新设备状态
     */
    boolean updateInstanceStatus(Integer id, Integer status);

    /**
     * 更新设备状况
     */
    boolean updateInstanceCondition(Integer id, Integer condition);

    /**
     * 根据序列号查询设备实例
     */
    CameraInstance getInstanceBySerialNumber(String serialNumber);

    /**
     * 获取设备实例详情（包含型号和品牌信息）
     */
    CameraInstance getInstanceDetail(Integer id);

    /**
     * 获取设备实例统计信息
     */
    Map<String, Object> getInstanceStats(Integer modelId);

    /**
     * 批量创建设备实例（用于多实例模式）
     */
    boolean batchCreateInstances(Integer modelId, List<CameraInstance> instances);

    /**
     * 检查序列号是否唯一
     */
    boolean isSerialNumberUnique(String serialNumber, Integer excludeId);
}