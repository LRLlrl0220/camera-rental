package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.Brand;
import com.shiguang.camera.mapper.BrandMapper;
import com.shiguang.camera.service.BrandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    /**
     * 获取所有品牌列表（按排序号排序）
     */
    @Override
    public List<Brand> getAllBrands() {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order");
        queryWrapper.orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取所有启用的品牌列表
     */
    @Override
    public List<Brand> getAllEnabledBrands() {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 状态为启用
        queryWrapper.orderByAsc("sort_order");
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    /**
     * 添加品牌
     */
    @Override
    @Transactional
    public boolean addBrand(Brand brand) {
        // 设置默认值
        if (brand.getStatus() == null) {
            brand.setStatus(1); // 默认启用
        }
        if (brand.getSortOrder() == null) {
            brand.setSortOrder(0); // 默认排序为0
        }

        return save(brand);
    }

    /**
     * 更新品牌
     */
    @Override
    @Transactional
    public boolean updateBrand(Brand brand) {
        return updateById(brand);
    }

    /**
     * 删除品牌（物理删除）
     */
    @Override
    @Transactional
    public boolean deleteBrand(Integer id) {
        return removeById(id); // 直接物理删除
    }

    /**
     * 上传品牌Logo
     */
    @Override
    @Transactional
    public boolean uploadLogo(Integer id, String logoUrl) {
        if (logoUrl == null || logoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Logo URL不能为空");
        }

        Brand brand = getById(id);
        if (brand == null) {
            throw new IllegalArgumentException("品牌不存在");
        }

        brand.setLogo(logoUrl);
        return updateById(brand);
    }

    /**
     * 获取品牌统计信息
     */
    @Override
    public Map<String, Object> getBrandStats() {
        // 品牌总数
        Long totalBrands = lambdaQuery().count();

        // 启用的品牌数
        Long enabledBrands = lambdaQuery()
                .eq(Brand::getStatus, 1)
                .count();

        // 停用的品牌数
        Long disabledBrands = lambdaQuery()
                .eq(Brand::getStatus, 0)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBrands", totalBrands);
        stats.put("enabledBrands", enabledBrands);
        stats.put("disabledBrands", disabledBrands);
        stats.put("recommendedBrands", 0); // 固定为0，因为没有推荐功能

        return stats;
    }

    /**
     * 根据名称查询品牌
     */
    @Override
    public Brand getBrandByName(String name) {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }

    /**
     * 获取启用的品牌列表（用于下拉选择）
     */
    @Override
    public List<Brand> getActiveBrands() {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 状态为启用
        queryWrapper.orderByAsc("sort_order");
        return list(queryWrapper);
    }
}