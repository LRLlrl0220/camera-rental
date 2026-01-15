package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM `order` WHERE reservation_id = #{reservationId}")
    Order selectByReservationId(@Param("reservationId") Integer reservationId);

    @Select("SELECT * FROM `order` WHERE status = #{status} ORDER BY create_time DESC")
    List<Order> selectByStatus(@Param("status") Integer status);
}