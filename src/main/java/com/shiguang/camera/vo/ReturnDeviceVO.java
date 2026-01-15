package com.shiguang.camera.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReturnDeviceVO {
    private LocalDate actualReturnDate;
    private String notes;
    private List<DamageItem> damageItems;

    @Data
    public static class DamageItem {
        private String description;
        private List<String> images; // 图片URL列表
        private BigDecimal repairCost;
    }
}