package com.shiguang.camera.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreatePaymentVO {
    @NotNull(message = "预订ID不能为空")
    private Integer reservationId;

    @NotNull(message = "支付方式不能为空")
    private String paymentMethod; // wechat/alipay

    private BigDecimal amount; // 可选，如果不传则从预订中获取
}