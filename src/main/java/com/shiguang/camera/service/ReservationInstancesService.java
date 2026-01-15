package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.ReservationInstances;

import java.util.List;

public interface ReservationInstancesService extends IService<ReservationInstances> {

    // 批量创建预订与设备实例的关联
    boolean batchCreate(Integer reservationId, List<Integer> instanceIds);

    // 根据预订ID获取关联的设备实例
    List<ReservationInstances> getByReservationId(Integer reservationId);
}