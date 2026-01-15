package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.Order;
import com.shiguang.camera.vo.CreateOrderVO;
import com.shiguang.camera.vo.ReturnDeviceVO;

import java.util.List;

public interface OrderService extends IService<Order> {

    // 创建订单
    Order createOrder(CreateOrderVO vo);

    // 根据预订创建订单
    Order createOrderFromReservation(Integer reservationId);

    // 确认取货
    boolean confirmPickup(Integer orderId, Integer adminId);

    // 确认归还
    boolean confirmReturn(Integer orderId, ReturnDeviceVO vo, Integer adminId);

    // 取消订单
    boolean cancelOrder(Integer orderId, String reason);

    // 获取用户订单列表
    List<Order> getUserOrders(Integer userId);

    // 获取订单详情
    Order getOrderDetail(Integer orderId);

    /**
     * 获取所有订单（管理员专用）
     * @param status 订单状态（可选）
     * @return 订单列表
     */
    List<Order> getAllOrders(Integer status);
}