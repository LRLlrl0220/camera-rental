package com.shiguang.camera.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING(0, "待支付"),
    WAITING_CONFIRM(1, "支付成功待确认"),
    CONFIRMED(2, "已确认"),
    REJECTED(3, "已拒绝"),
    REFUNDED(4, "已退款");

    private final Integer code;
    private final String description;

    PaymentStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentStatus fromCode(Integer code) {
        if (code == null) return null;
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的支付状态码: " + code);
    }
}