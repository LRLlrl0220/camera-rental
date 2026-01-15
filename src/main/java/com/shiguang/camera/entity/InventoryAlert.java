package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("inventory_alert")
public class InventoryAlert {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 设备型号ID
     */
    private Integer modelId;

    /**
     * 报警类型：low_stock, out_of_stock
     */
    private String alertType;

    /**
     * 消息
     */
    private String message;

    /**
     * 优先级：1-低 2-中 3-高
     */
    private Integer priority;

    /**
     * 状态：0-未处理 1-已处理 2-已忽略
     */
    private Integer status;

    /**
     * 处理人
     */
    private Integer resolvedBy;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}