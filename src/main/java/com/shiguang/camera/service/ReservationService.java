package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.Reservation;
import com.shiguang.camera.vo.ReservationCreateVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService extends IService<Reservation> {

    // 创建预订
    Reservation createReservation(ReservationCreateVO vo, Integer userId);

    // 确认预订
    boolean confirmReservation(Integer reservationId, Integer adminId);

    // 取消预订
    boolean cancelReservation(Integer reservationId, Integer userId, boolean isAdmin);

    // 完成预订
    boolean completeReservation(Integer reservationId, Integer adminId);

    // 检查时间冲突
    boolean checkTimeConflict(Integer modelId, LocalDate startDate, LocalDate endDate,
                              LocalTime startTime, LocalTime endTime, Integer excludeId);

    // 检查库存
    boolean checkInventory(Integer modelId, Integer quantity,
                           LocalDate startDate, LocalDate endDate);

    // 获取用户预订列表
    List<Reservation> getUserReservations(Integer userId);

    // 获取待处理预订列表
    List<Reservation> getPendingReservations();

    // 获取预订详情（包含关联的设备实例）
    Reservation getReservationDetail(Integer reservationId);
}