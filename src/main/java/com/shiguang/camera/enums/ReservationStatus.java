package com.shiguang.camera.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    已预订(0, "已预订"),
    已确认(1, "已确认"),
    已取消(2, "已取消"),
    已完成(3, "已完成");

    private final Integer code;
    private final String description;

    ReservationStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReservationStatus fromCode(Integer code) {
        if (code == null) return null;
        for (ReservationStatus status : ReservationStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的预订状态码: " + code);
    }
}