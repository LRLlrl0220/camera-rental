package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.Brand;
import com.shiguang.camera.entity.CameraModel;
import com.shiguang.camera.mapper.CameraModelMapper;
import com.shiguang.camera.service.BrandService;
import com.shiguang.camera.service.CameraModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CameraModelServiceImpl extends ServiceImpl<CameraModelMapper, CameraModel>
        implements CameraModelService {

    @Autowired
    private BrandService brandService;

    @Override
    public List<CameraModel> getAllEnabledModels() {
        return baseMapper.selectEnabledModels();
    }

    @Override
    public List<CameraModel> getModelsByBrandId(Integer brandId) {
        return baseMapper.selectByBrandId(brandId);
    }

    @Override
    public List<CameraModel> searchModels(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEnabledModels();
        }
        return baseMapper.searchModels(keyword.trim());
    }

    @Override
    public IPage<CameraModel> getModelPage(Integer page, Integer size,
                                           Integer brandId, String keyword, Integer status) {
        Page<CameraModel> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CameraModel> queryWrapper = new LambdaQueryWrapper<>();

        // 品牌筛选
        if (brandId != null) {
            queryWrapper.eq(CameraModel::getBrandId, brandId);
        }

        // 状态筛选
        if (status != null) {
            queryWrapper.eq(CameraModel::getStatus, status);
        } else {
            queryWrapper.eq(CameraModel::getStatus, 1); // 默认只查启用的
        }

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            String keywordTrimmed = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(CameraModel::getName, keywordTrimmed)
                    .or()
                    .like(CameraModel::getModel, keywordTrimmed)
                    .or()
                    .like(CameraModel::getDescription, keywordTrimmed)
            );
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(CameraModel::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    @Transactional
    public boolean addCameraModel(CameraModel cameraModel) {
        // 验证必填字段
        if (cameraModel.getName() == null || cameraModel.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("相机型号名称不能为空");
        }
        if (cameraModel.getDailyPrice() == null) {
            throw new IllegalArgumentException("日租金不能为空");
        }

        // 设置默认值
        if (cameraModel.getStatus() == null) {
            cameraModel.setStatus(1); // 默认启用
        }
        if (cameraModel.getInventoryType() == null) {
            cameraModel.setInventoryType(0); // 默认单实例模式
        }

        // 验证品牌是否存在
        if (cameraModel.getBrandId() != null) {
            Brand brand = brandService.getById(cameraModel.getBrandId());
            if (brand == null) {
                throw new IllegalArgumentException("品牌不存在");
            }
        }

        return save(cameraModel);
    }

    @Override
    @Transactional
    public boolean updateCameraModel(CameraModel cameraModel) {
        if (cameraModel.getId() == null) {
            throw new IllegalArgumentException("相机型号ID不能为空");
        }

        // 验证品牌是否存在
        if (cameraModel.getBrandId() != null) {
            Brand brand = brandService.getById(cameraModel.getBrandId());
            if (brand == null) {
                throw new IllegalArgumentException("品牌不存在");
            }
        }

        return updateById(cameraModel);
    }

    @Override
    @Transactional
    public boolean toggleModelStatus(Integer id, Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值必须是0或1");
        }

        CameraModel cameraModel = getById(id);
        if (cameraModel == null) {
            throw new IllegalArgumentException("相机型号不存在");
        }

        cameraModel.setStatus(status);
        return updateById(cameraModel);
    }

    @Override
    public CameraModel getModelDetail(Integer id) {
        CameraModel cameraModel = getById(id);
        if (cameraModel == null) {
            return null;
        }

        // 如果品牌ID存在，查询品牌信息
        if (cameraModel.getBrandId() != null) {
            Brand brand = brandService.getById(cameraModel.getBrandId());
            cameraModel.setBrand(brand);
        }

        return cameraModel;
    }

    @Override
    public Map<String, Object> getModelStats() {
        // 相机型号总数
        Long totalModels = lambdaQuery().count();

        // 启用的相机型号数
        Long enabledModels = lambdaQuery()
                .eq(CameraModel::getStatus, 1)
                .count();

        // 停用的相机型号数
        Long disabledModels = lambdaQuery()
                .eq(CameraModel::getStatus, 0)
                .count();

        // 按品牌统计 - 使用Mapper中的自定义方法
        List<Map<String, Object>> brandStats = baseMapper.selectBrandStats();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalModels", totalModels);
        stats.put("enabledModels", enabledModels);
        stats.put("disabledModels", disabledModels);
        stats.put("brandStats", brandStats);

        return stats;
    }
}