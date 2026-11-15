package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    void insertBatch(@Param("orderDetails") ArrayList<OrderDetail> orderDetails);

    /**
     * 根据订单id查询订单明细
     */
    List<OrderDetail> getByOrderId(@Param("orderId") Long orderId);
}
