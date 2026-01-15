package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.Brand;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BrandMapper extends BaseMapper<Brand> {
    // 可以添加自定义查询方法
}