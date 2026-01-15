package com.shiguang.camera.controller;

import com.shiguang.camera.annotation.*;
import com.shiguang.camera.entity.Reservation;
import com.shiguang.camera.service.ReservationService;
import com.shiguang.camera.vo.ReservationCreateVO;
import com.shiguang.camera.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 创建预订 - 需要实名认证的用户
     */
    @PostMapping("/create")
    @RequireVerifiedUser
    public Result<Reservation> createReservation(@Validated @RequestBody ReservationCreateVO vo,
                                                 HttpServletRequest request) {
        // 保持原有逻辑：从request获取userId
        Integer userId = (Integer) request.getAttribute("userId");
        Reservation reservation = reservationService.createReservation(vo, userId);
        return Result.success(reservation);
    }

    /**
     * 管理员确认预订 - 需要管理员权限
     */
    @PutMapping("/{id}/confirm")
    @RequireAdmin
    public Result<Boolean> confirmReservation(@PathVariable Integer id,
                                              HttpServletRequest request) {
        // 保持原有逻辑：从request获取管理员ID
        Integer adminId = (Integer) request.getAttribute("userId");
        boolean success = reservationService.confirmReservation(id, adminId);
        return Result.success(success);
    }

    /**
     * 取消预订 - 需要登录，原有逻辑处理用户/管理员权限
     */
    @PutMapping("/{id}/cancel")
    @RequireLogin
    public Result<Boolean> cancelReservation(@PathVariable Integer id,
                                             HttpServletRequest request) {
        // 保持原有逻辑：获取用户ID和isAdmin标志
        Integer userId = (Integer) request.getAttribute("userId");
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
        boolean success = reservationService.cancelReservation(id, userId, isAdmin);
        return Result.success(success);
    }

    /**
     * 管理员完成预订 - 需要管理员权限
     */
    @PutMapping("/{id}/complete")
    @RequireAdmin
    public Result<Boolean> completeReservation(@PathVariable Integer id,
                                               HttpServletRequest request) {
        // 保持原有逻辑：从request获取管理员ID
        Integer adminId = (Integer) request.getAttribute("userId");
        boolean success = reservationService.completeReservation(id, adminId);
        return Result.success(success);
    }

    /**
     * 获取用户自己的预订列表 - 需要登录
     */
    @GetMapping("/my")
    @RequireLogin
    public Result<List<Reservation>> getMyReservations(HttpServletRequest request) {
        // 保持原有逻辑：从request获取userId
        Integer userId = (Integer) request.getAttribute("userId");
        List<Reservation> reservations = reservationService.getUserReservations(userId);
        return Result.success(reservations);
    }

    /**
     * 获取预订详情 - 不需要登录（如果允许查看），保持原有逻辑
     */
    @GetMapping("/{id}")
    public Result<Reservation> getReservationDetail(@PathVariable Integer id) {
        Reservation reservation = reservationService.getReservationDetail(id);
        return Result.success(reservation);
    }

    /**
     * 管理员获取待处理预订列表 - 需要管理员权限
     */
    @GetMapping("/pending")
    @RequireAdmin
    public Result<List<Reservation>> getPendingReservations() {
        List<Reservation> reservations = reservationService.getPendingReservations();
        return Result.success(reservations);
    }

    /**
     * 检查时间冲突 - 不需要登录，保持原有逻辑
     */
    @GetMapping("/check-conflict")
    public Result<Boolean> checkTimeConflict(@RequestParam Integer modelId,
                                             @RequestParam String startDate,
                                             @RequestParam String endDate,
                                             @RequestParam String startTime,
                                             @RequestParam String endTime,
                                             @RequestParam(required = false) Integer excludeId) {
        boolean hasConflict = reservationService.checkTimeConflict(
                modelId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                LocalTime.parse(startTime),
                LocalTime.parse(endTime),
                excludeId
        );
        return Result.success(hasConflict);
    }

    /**
     * 检查库存 - 不需要登录，保持原有逻辑
     */
    @GetMapping("/check-inventory")
    public Result<Boolean> checkInventory(@RequestParam Integer modelId,
                                          @RequestParam Integer quantity,
                                          @RequestParam String startDate,
                                          @RequestParam String endDate) {
        boolean hasEnough = reservationService.checkInventory(
                modelId,
                quantity,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        return Result.success(hasEnough);
    }
}