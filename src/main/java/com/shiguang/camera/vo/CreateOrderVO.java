package com.shiguang.camera.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class CreateOrderVO {
    @NotNull(message = "预订ID不能为空")
    private Integer reservationId;

    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    private String realName;
    private String idCard;
}