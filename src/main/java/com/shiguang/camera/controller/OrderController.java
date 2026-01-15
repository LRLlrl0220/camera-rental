package com.shiguang.camera.controller;

import com.shiguang.camera.annotation.RequireAdmin;
import com.shiguang.camera.annotation.RequireLogin;
import com.shiguang.camera.annotation.RequireVerifiedUser;
import com.shiguang.camera.entity.Order;
import com.shiguang.camera.service.OrderService;
import com.shiguang.camera.vo.CreateOrderVO;
import com.shiguang.camera.vo.ReturnDeviceVO;
import com.shiguang.camera.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单 - 需要实名认证的用户才能下单
     */
    @PostMapping
    @RequireVerifiedUser
    public Result<Order> createOrder(@Validated @RequestBody CreateOrderVO vo,
                                     HttpServletRequest request) {
        // 原有逻辑完全不变
        Integer userId = (Integer) request.getAttribute("userId");
        vo.setUserId(userId);
        Order order = orderService.createOrder(vo);
        return Result.success(order);
    }

    /**
     * 获取我的订单列表 - 需要登录的用户
     */
    @GetMapping("/my")
    @RequireLogin
    public Result<List<Order>> getMyOrders(HttpServletRequest request) {
        // 原有逻辑完全不变
        Integer userId = (Integer) request.getAttribute("userId");
        List<Order> orders = orderService.getUserOrders(userId);
        return Result.success(orders);
    }

    /**
     * 获取订单详情 - 需要登录，原有业务层会检查权限
     */
    @GetMapping("/{id}")
    @RequireLogin
    public Result<Order> getOrderDetail(@PathVariable Integer id) {
        // 原有逻辑完全不变，业务层会检查订单归属
        Order order = orderService.getOrderDetail(id);
        return Result.success(order);
    }

    /**
     * 确认取货 - 需要管理员权限
     */
    @PutMapping("/{id}/pickup")
    @RequireAdmin
    public Result<Boolean> confirmPickup(@PathVariable Integer id,
                                         HttpServletRequest request) {
        // 原有逻辑完全不变
        Integer adminId = (Integer) request.getAttribute("userId");
        boolean success = orderService.confirmPickup(id, adminId);
        return Result.success(success);
    }

    /**
     * 确认归还 - 需要管理员权限
     */
    @PutMapping("/{id}/return")
    @RequireAdmin
    public Result<Boolean> confirmReturn(@PathVariable Integer id,
                                         @Validated @RequestBody ReturnDeviceVO vo,
                                         HttpServletRequest request) {
        // 原有逻辑完全不变
        Integer adminId = (Integer) request.getAttribute("userId");
        boolean success = orderService.confirmReturn(id, vo, adminId);
        return Result.success(success);
    }

    /**
     * 取消订单 - 需要登录
     */
    @PutMapping("/{id}/cancel")
    @RequireLogin
    public Result<Boolean> cancelOrder(@PathVariable Integer id,
                                       @RequestParam String reason,
                                       HttpServletRequest request) {
        // 原有逻辑完全不变
        Integer userId = (Integer) request.getAttribute("userId");

        // 注意：这里不进行数据归属检查，因为业务层已经处理了
        // 如果需要更严格的控制，可以在这里添加额外的检查
        boolean success = orderService.cancelOrder(id, reason);
        return Result.success(success);
    }

    /**
     * 获取所有订单 - 需要管理员权限（需要先在OrderService中添加此方法）
     * 注意：你的OrderService接口中目前没有这个方法
     * 如果你需要这个功能，需要在OrderService中添加
     */
    @GetMapping
    @RequireAdmin
    public Result<List<Order>> getAllOrders(@RequestParam(required = false) Integer status) {
        // 由于你的OrderService中没有这个方法，暂时注释掉
        // List<Order> orders = orderService.getAllOrders(status);
        // return Result.success(orders);

        // 临时返回空列表，实际使用时需要实现这个方法
        return Result.success(null, "功能待实现");
    }
}