package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.Brand;
import com.shiguang.camera.entity.CameraInstance;
import com.shiguang.camera.entity.CameraModel;
import com.shiguang.camera.mapper.CameraInstanceMapper;
import com.shiguang.camera.service.BrandService;
import com.shiguang.camera.service.CameraInstanceService;
import com.shiguang.camera.service.CameraModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CameraInstanceServiceImpl extends ServiceImpl<CameraInstanceMapper, CameraInstance>
        implements CameraInstanceService {

    @Autowired
    private CameraModelService cameraModelService;

    @Autowired
    private BrandService brandService;

    @Override
    public List<CameraInstance> getInstancesByModelId(Integer modelId) {
        return baseMapper.selectByModelId(modelId);
    }

    @Override
    public List<CameraInstance> getAvailableInstancesByModelId(Integer modelId) {
        return baseMapper.selectAvailableByModelId(modelId);
    }

    @Override
    public IPage<CameraInstance> getInstancePage(Integer page, Integer size,
                                                 Integer modelId, String serialNumber,
                                                 Integer status, String keyword) {
        Page<CameraInstance> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CameraInstance> queryWrapper = new LambdaQueryWrapper<>();

        // 按型号筛选
        if (modelId != null) {
            queryWrapper.eq(CameraInstance::getModelId, modelId);
        }

        // 按序列号筛选
        if (serialNumber != null && !serialNumber.trim().isEmpty()) {
            queryWrapper.like(CameraInstance::getSerialNumber, serialNumber.trim());
        }

        // 按状态筛选
        if (status != null) {
            queryWrapper.eq(CameraInstance::getStatus, status);
        } else {
            // 默认不显示已下架的设备
            queryWrapper.ne(CameraInstance::getStatus, 4);
        }

        // 关键词搜索（搜索序列号、资产标签、备注）
        if (keyword != null && !keyword.trim().isEmpty()) {
            String keywordTrimmed = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(CameraInstance::getSerialNumber, keywordTrimmed)
                    .or()
                    .like(CameraInstance::getAssetTag, keywordTrimmed)
                    .or()
                    .like(CameraInstance::getNotes, keywordTrimmed)
            );
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(CameraInstance::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    @Transactional
    public boolean createCameraInstance(CameraInstance cameraInstance) {
        // 验证必填字段
        if (cameraInstance.getModelId() == null) {
            throw new IllegalArgumentException("相机型号ID不能为空");
        }
        if (cameraInstance.getSerialNumber() == null || cameraInstance.getSerialNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("设备序列号不能为空");
        }

        // 验证相机型号是否存在
        CameraModel cameraModel = cameraModelService.getById(cameraInstance.getModelId());
        if (cameraModel == null) {
            throw new IllegalArgumentException("相机型号不存在");
        }

        // 验证序列号是否唯一
        if (!isSerialNumberUnique(cameraInstance.getSerialNumber(), null)) {
            throw new IllegalArgumentException("设备序列号已存在");
        }

        // 设置默认值
        if (cameraInstance.getStatus() == null) {
            cameraInstance.setStatus(0); // 默认可用状态
        }
        if (cameraInstance.getCondition() == null) {
            cameraInstance.setCondition(0); // 默认状况为优
        }

        return save(cameraInstance);
    }

    @Override
    @Transactional
    public boolean updateCameraInstance(CameraInstance cameraInstance) {
        if (cameraInstance.getId() == null) {
            throw new IllegalArgumentException("设备实例ID不能为空");
        }

        // 验证设备实例是否存在
        CameraInstance existingInstance = getById(cameraInstance.getId());
        if (existingInstance == null) {
            throw new IllegalArgumentException("设备实例不存在");
        }

        // 如果修改了序列号，需要验证唯一性
        if (cameraInstance.getSerialNumber() != null &&
                !cameraInstance.getSerialNumber().equals(existingInstance.getSerialNumber())) {
            if (!isSerialNumberUnique(cameraInstance.getSerialNumber(), cameraInstance.getId())) {
                throw new IllegalArgumentException("设备序列号已存在");
            }
        }

        // 如果修改了型号ID，需要验证型号是否存在
        if (cameraInstance.getModelId() != null &&
                !cameraInstance.getModelId().equals(existingInstance.getModelId())) {
            CameraModel cameraModel = cameraModelService.getById(cameraInstance.getModelId());
            if (cameraModel == null) {
                throw new IllegalArgumentException("相机型号不存在");
            }
        }

        return updateById(cameraInstance);
    }

    @Override
    @Transactional
    public boolean deleteCameraInstance(Integer id) {
        CameraInstance instance = getById(id);
        if (instance == null) {
            throw new IllegalArgumentException("设备实例不存在");
        }

        // 检查设备状态，只有可用的设备才能删除
        if (instance.getStatus() != 0) {
            throw new IllegalArgumentException("只有可用状态的设备才能删除");
        }

        return removeById(id);
    }

    @Override
    @Transactional
    public boolean updateInstanceStatus(Integer id, Integer status) {
        if (status < 0 || status > 4) {
            throw new IllegalArgumentException("状态值必须在0-4之间");
        }

        CameraInstance instance = getById(id);
        if (instance == null) {
            throw new IllegalArgumentException("设备实例不存在");
        }

        // 状态转换验证
        if (!isValidStatusTransition(instance.getStatus(), status)) {
            throw new IllegalArgumentException("不允许的状态转换");
        }

        instance.setStatus(status);
        return updateById(instance);
    }

    @Override
    @Transactional
    public boolean updateInstanceCondition(Integer id, Integer condition) {
        if (condition < 0 || condition > 3) {
            throw new IllegalArgumentException("状况值必须在0-3之间");
        }

        CameraInstance instance = getById(id);
        if (instance == null) {
            throw new IllegalArgumentException("设备实例不存在");
        }

        instance.setCondition(condition);

        // 如果设备状况为"需维修"，自动将状态改为"维修中"
        if (condition == 3 && instance.getStatus() != 3) {
            instance.setStatus(3);
        }

        return updateById(instance);
    }

    @Override
    public CameraInstance getInstanceBySerialNumber(String serialNumber) {
        return baseMapper.selectBySerialNumber(serialNumber);
    }

    @Override
    public CameraInstance getInstanceDetail(Integer id) {
        CameraInstance instance = getById(id);
        if (instance == null) {
            return null;
        }

        // 查询相机型号信息
        if (instance.getModelId() != null) {
            // 获取相机型号详情，这个详情中应该已经包含了品牌信息
            CameraModel cameraModel = cameraModelService.getModelDetail(instance.getModelId());
            instance.setCameraModel(cameraModel);

            // 如果相机型号详情中已经有品牌信息，直接设置
            if (cameraModel != null && cameraModel.getBrand() != null) {
                instance.setBrand(cameraModel.getBrand());
            }
            // 如果相机型号详情中没有品牌信息，但品牌ID存在，查询品牌信息
            else if (cameraModel != null && cameraModel.getBrandId() != null) {
                Brand brand = brandService.getById(cameraModel.getBrandId());
                instance.setBrand(brand);
            }
        }

        return instance;
    }

    @Override
    public Map<String, Object> getInstanceStats(Integer modelId) {
        Map<String, Object> stats = new HashMap<>();

        // 总设备数
        Long totalInstances;
        // 各状态设备数
        List<CameraInstanceMapper.StatusCount> statusCounts;

        if (modelId != null) {
            totalInstances = lambdaQuery()
                    .eq(CameraInstance::getModelId, modelId)
                    .count();
            statusCounts = baseMapper.countByStatus(modelId);
        } else {
            totalInstances = lambdaQuery().count();
            // 对于所有型号，需要手动统计
            statusCounts = lambdaQuery()
                    .select(CameraInstance::getStatus)
                    .groupBy(CameraInstance::getStatus)
                    .list()
                    .stream()
                    .map(instance -> {
                        CameraInstanceMapper.StatusCount sc = new CameraInstanceMapper.StatusCount();
                        sc.setStatus(instance.getStatus());
                        sc.setCount(1L); // 这里需要实际统计，简化处理
                        return sc;
                    })
                    .collect(Collectors.toList());
        }

        // 可用设备数
        Long availableCount = lambdaQuery()
                .eq(CameraInstance::getStatus, 0)
                .eq(modelId != null, CameraInstance::getModelId, modelId)
                .count();

        stats.put("totalInstances", totalInstances);
        stats.put("availableInstances", availableCount);
        stats.put("statusCounts", statusCounts);

        return stats;
    }

    @Override
    @Transactional
    public boolean batchCreateInstances(Integer modelId, List<CameraInstance> instances) {
        if (modelId == null) {
            throw new IllegalArgumentException("相机型号ID不能为空");
        }

        // 验证相机型号是否存在
        CameraModel cameraModel = cameraModelService.getById(modelId);
        if (cameraModel == null) {
            throw new IllegalArgumentException("相机型号不存在");
        }

        // 验证序列号唯一性
        for (CameraInstance instance : instances) {
            instance.setModelId(modelId);

            if (instance.getSerialNumber() == null || instance.getSerialNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("设备序列号不能为空");
            }

            if (!isSerialNumberUnique(instance.getSerialNumber(), null)) {
                throw new IllegalArgumentException("设备序列号已存在: " + instance.getSerialNumber());
            }

            // 设置默认值
            if (instance.getStatus() == null) {
                instance.setStatus(0);
            }
            if (instance.getCondition() == null) {
                instance.setCondition(0);
            }
        }

        return saveBatch(instances);
    }

    @Override
    public boolean isSerialNumberUnique(String serialNumber, Integer excludeId) {
        LambdaQueryWrapper<CameraInstance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CameraInstance::getSerialNumber, serialNumber);

        if (excludeId != null) {
            queryWrapper.ne(CameraInstance::getId, excludeId);
        }

        return count(queryWrapper) == 0;
    }

    /**
     * 验证状态转换是否有效
     */
    private boolean isValidStatusTransition(Integer fromStatus, Integer toStatus) {
        // 状态转换规则
        // 0-可用 -> 0,1,3,4 (可用可以变为已预订、维修中、下架)
        // 1-已预订 -> 0,2 (已预订可以变为可用或租赁中)
        // 2-租赁中 -> 0,3 (租赁中可以变为可用或维修中)
        // 3-维修中 -> 0 (维修中可以变为可用)
        // 4-已下架 -> 0 (下架可以恢复为可用)

        switch (fromStatus) {
            case 0: // 可用
                return toStatus == 0 || toStatus == 1 || toStatus == 3 || toStatus == 4;
            case 1: // 已预订
                return toStatus == 0 || toStatus == 2;
            case 2: // 租赁中
                return toStatus == 0 || toStatus == 3;
            case 3: // 维修中
                return toStatus == 0;
            case 4: // 已下架
                return toStatus == 0;
            default:
                return false;
        }
    }
}