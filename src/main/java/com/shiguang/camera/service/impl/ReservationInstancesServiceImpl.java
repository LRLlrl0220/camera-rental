package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.ReservationInstances;
import com.shiguang.camera.mapper.ReservationInstancesMapper;
import com.shiguang.camera.service.ReservationInstancesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationInstancesServiceImpl extends ServiceImpl<ReservationInstancesMapper, ReservationInstances>
        implements ReservationInstancesService {

    private final ReservationInstancesMapper reservationInstancesMapper;

    @Override
    public boolean batchCreate(Integer reservationId, List<Integer> instanceIds) {
        try {
            for (Integer instanceId : instanceIds) {
                ReservationInstances ri = new ReservationInstances();
                ri.setReservationId(reservationId);
                ri.setInstanceId(instanceId);
                reservationInstancesMapper.insert(ri);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<ReservationInstances> getByReservationId(Integer reservationId) {
        return reservationInstancesMapper.selectList(
                new LambdaQueryWrapper<ReservationInstances>()
                        .eq(ReservationInstances::getReservationId, reservationId)
        );
    }
}