package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM payment WHERE payment_no = #{paymentNo}")
    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);

    @Select("SELECT * FROM payment WHERE reservation_id = #{reservationId} AND status != 3")
    Payment selectByReservationId(@Param("reservationId") Integer reservationId);

    @Select("SELECT * FROM payment WHERE status = #{status} ORDER BY create_time DESC")
    List<Payment> selectByStatus(@Param("status") Integer status);

    @Update("UPDATE payment SET status = #{status}, confirmed_time = #{confirmedTime}, confirmed_by = #{confirmedBy} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id,
                     @Param("status") Integer status,
                     @Param("confirmedTime") LocalDateTime confirmedTime,
                     @Param("confirmedBy") Integer confirmedBy);
}