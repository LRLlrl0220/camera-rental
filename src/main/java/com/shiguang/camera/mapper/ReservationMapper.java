package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalTime;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    /**
     * 检查时间段内的预订冲突
     * 注意：这里假设预订是按天计算的，不考虑具体时间段的冲突
     */
    @Select("SELECT SUM(quantity) FROM reservation " +
            "WHERE model_id = #{modelId} " +
            "AND status IN (0, 1) " + // 原来是：AND status IN ('PENDING', 'CONFIRMED')
            "AND NOT (end_date < #{startDate} OR start_date > #{endDate}) " +
            "AND (id != #{excludeId} OR #{excludeId} IS NULL)")
    Integer checkTimeConflict(@Param("modelId") Integer modelId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("startTime") LocalTime startTime,
                              @Param("endTime") LocalTime endTime,
                              @Param("excludeId") Integer excludeId);

    /**

    /**
     * 获取特定时间段内的已预订数量
     */
    @Select("SELECT SUM(quantity) FROM reservation " +
            "WHERE model_id = #{modelId} " +
            "AND status IN (0, 1) " + // 原来是：AND status IN ('PENDING', 'CONFIRMED')
            "AND NOT (end_date < #{startDate} OR start_date > #{endDate})")
    Integer getBookedQuantity(@Param("modelId") Integer modelId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);
}