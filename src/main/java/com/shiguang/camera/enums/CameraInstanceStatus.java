package com.shiguang.camera.enums;

import lombok.Getter;

@Getter
public enum CameraInstanceStatus {
    AVAILABLE(0, "可用"),
    RESERVED(1, "已预订"),      // 支付成功后的状态
    RENTING(2, "租赁中"),       // 取货后的状态
    MAINTENANCE(3, "维修中"),
    OFFLINE(4, "已下架"),       // 这里叫做 OFFLINE，不是 OFF_SHELF
    PRE_RESERVED(5, "预占中");  // 新增：预订创建但未支付

    private final Integer code;
    private final String description;

    CameraInstanceStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CameraInstanceStatus fromCode(Integer code) {
        if (code == null) return null;
        for (CameraInstanceStatus status : CameraInstanceStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的设备实例状态码: " + code);
    }
}