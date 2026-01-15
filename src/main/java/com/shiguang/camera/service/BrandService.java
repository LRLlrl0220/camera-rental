package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.Brand;
import java.util.List;
import java.util.Map;

public interface BrandService extends IService<Brand> {

    /**
     * 获取所有品牌列表（按排序号排序）
     */
    List<Brand> getAllBrands();

    /**
     * 获取所有启用的品牌列表
     */
    List<Brand> getAllEnabledBrands();

    /**
     * 添加品牌
     */
    boolean addBrand(Brand brand);

    /**
     * 更新品牌
     */
    boolean updateBrand(Brand brand);

    /**
     * 删除品牌（物理删除）
     */
    boolean deleteBrand(Integer id);

    /**
     * 上传品牌Logo
     */
    boolean uploadLogo(Integer id, String logoUrl);

    /**
     * 获取品牌统计信息
     */
    Map<String, Object> getBrandStats();

    /**
     * 根据名称查询品牌
     */
    Brand getBrandByName(String name);

    /**
     * 获取启用的品牌列表（用于下拉选择）
     */
    List<Brand> getActiveBrands();
}