package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("camera_instance")
public class CameraInstance {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 设备型号ID
     */
    private Integer modelId;

    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 资产标签
     */
    private String assetTag;

    /**
     * 状态：0-可用 1-已预订 2-租赁中 3-维修中 4-已下架
     */
    private Integer status;

    /**
     * 设备状况：0-优 1-良 2-中 3-需维修
     * 使用反引号转义，因为condition是MySQL关键字
     */
    @TableField("`condition`")
    private Integer condition;

    /**
     * 存放位置信息（JSON格式存储）
     */
    private String location;

    /**
     * 备注
     */
    private String notes;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    /**
     * 保修到期
     */
    private LocalDate warrantyExpiry;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ====== 非数据库字段，用于关联查询 ======

    /**
     * 相机型号信息（关联查询）
     */
    @TableField(exist = false)
    private CameraModel cameraModel;

    /**
     * 品牌信息（通过cameraModel关联）
     */
    @TableField(exist = false)
    private Brand brand;
}