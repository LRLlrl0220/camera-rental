package com.shiguang.camera.vo;

import lombok.Data;

@Data
//用于查询
public class ReservationQueryVO {
    private Integer userId;
    private Integer modelId;
    private Integer status;
    private String startDate;
    private String endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}