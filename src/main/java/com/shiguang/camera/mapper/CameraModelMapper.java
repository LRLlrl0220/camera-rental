package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.CameraModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CameraModelMapper extends BaseMapper<CameraModel> {

    /**
     * 获取所有启用的相机型号
     */
    @Select("SELECT * FROM camera_model WHERE status = 1 ORDER BY create_time DESC")
    List<CameraModel> selectEnabledModels();

    /**
     * 根据品牌ID获取相机型号
     */
    @Select("SELECT * FROM camera_model WHERE brand_id = #{brandId} AND status = 1")
    List<CameraModel> selectByBrandId(@Param("brandId") Integer brandId);

    /**
     * 搜索相机型号
     */
    @Select("SELECT * FROM camera_model WHERE status = 1 AND " +
            "(name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "model LIKE CONCAT('%', #{keyword}, '%') OR " +
            "description LIKE CONCAT('%', #{keyword}, '%'))")
    List<CameraModel> searchModels(@Param("keyword") String keyword);

    /**
     * 分页查询相机型号（带品牌信息）
     */
    @Select("SELECT cm.*, b.name as brand_name, b.logo as brand_logo " +
            "FROM camera_model cm " +
            "LEFT JOIN brand b ON cm.brand_id = b.id " +
            "WHERE cm.status = 1 " +
            "ORDER BY cm.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<CameraModel> selectPageWithBrand(@Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    /**
     * 按品牌统计相机型号数量
     */
    @Select("SELECT b.name as brand_name, COUNT(*) as model_count " +
            "FROM camera_model cm " +  // 这里修复了，原来是"camera_model.cn"
            "LEFT JOIN brand b ON cm.brand_id = b.id " +
            "WHERE cm.status = 1 " +
            "GROUP BY cm.brand_id " +
            "ORDER BY model_count DESC")
    List<Map<String, Object>> selectBrandStats();
}