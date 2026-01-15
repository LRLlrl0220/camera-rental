package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.CameraInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
//设备实例
public interface CameraInstanceMapper extends BaseMapper<CameraInstance> {

    /**
     * 根据型号ID获取设备实例
     */
    @Select("SELECT * FROM camera_instance WHERE model_id = #{modelId} AND status != 4")
    List<CameraInstance> selectByModelId(@Param("modelId") Integer modelId);

    /**
     * 获取可用设备实例
     */
    @Select("SELECT * FROM camera_instance WHERE model_id = #{modelId} AND status = 0")
    List<CameraInstance> selectAvailableByModelId(@Param("modelId") Integer modelId);

    /**
     * 统计各状态的设备数量
     */
    @Select("SELECT status, COUNT(*) as count FROM camera_instance WHERE model_id = #{modelId} GROUP BY status")
    List<StatusCount> countByStatus(@Param("modelId") Integer modelId);

    /**
     * 根据序列号查询
     */
    @Select("SELECT * FROM camera_instance WHERE serial_number = #{serialNumber}")
    CameraInstance selectBySerialNumber(@Param("serialNumber") String serialNumber);

    /**
     * 统计特定型号的设备实例数量（排除特定状态）
     */
    @Select("SELECT COUNT(*) FROM camera_instance WHERE model_id = #{modelId} AND status != #{excludeStatus}")
    Long countByModelIdAndStatusNot(@Param("modelId") Integer modelId, @Param("excludeStatus") Integer excludeStatus);

    // 状态统计内部类
    class StatusCount {
        private Integer status;
        private Long count;

        // getter/setter
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }

        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}