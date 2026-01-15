package com.shiguang.camera.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING_PAYMENT(0, "待支付"),
    PAID_WAITING_PICKUP(1, "已支付待取机"),
    RENTING(2, "租赁中"),
    RETURNED_WAITING_REFUND(3, "已归还待退款"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatus fromCode(Integer code) {
        if (code == null) return null;
        for (OrderStatus status : OrderStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态码: " + code);
    }
}