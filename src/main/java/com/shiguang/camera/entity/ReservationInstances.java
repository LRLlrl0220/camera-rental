package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reservation_instances")
public class ReservationInstances {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("reservation_id")
    private Integer reservationId;

    @TableField("instance_id")
    private Integer instanceId;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ====== 非数据库字段，用于关联查询 ======

    /**
     * 预订信息
     */
    @TableField(exist = false)
    private Reservation reservation;

    /**
     * 设备实例信息
     */
    @TableField(exist = false)
    private CameraInstance cameraInstance;
}