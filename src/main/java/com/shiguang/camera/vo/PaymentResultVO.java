package com.shiguang.camera.vo;

import lombok.Data;

@Data
public class PaymentResultVO {
    private Boolean success;
    private String message;
    private String paymentNo;
    private Integer status;
    private String qrCodeUrl; // 模拟支付二维码URL
}