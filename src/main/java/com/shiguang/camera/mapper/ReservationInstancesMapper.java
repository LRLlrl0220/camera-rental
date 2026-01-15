package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.ReservationInstances;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReservationInstancesMapper extends BaseMapper<ReservationInstances> {

    /**
     * 根据预订ID获取关联的设备实例
     */
    @Select("SELECT * FROM reservation_instances WHERE reservation_id = #{reservationId}")
    List<ReservationInstances> selectByReservationId(@Param("reservationId") Integer reservationId);

    /**
     * 根据实例ID查询预订关联
     */
    @Select("SELECT * FROM reservation_instances WHERE instance_id = #{instanceId}")
    List<ReservationInstances> selectByInstanceId(@Param("instanceId") Integer instanceId);

    /**
     * 根据预订ID批量删除关联
     */
    @Select("DELETE FROM reservation_instances WHERE reservation_id = #{reservationId}")
    int deleteByReservationId(@Param("reservationId") Integer reservationId);

    /**
     * 统计预订的设备实例数量
     */
    @Select("SELECT COUNT(*) FROM reservation_instances WHERE reservation_id = #{reservationId}")
    int countByReservationId(@Param("reservationId") Integer reservationId);
}