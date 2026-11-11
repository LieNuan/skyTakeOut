package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface OrderDetailMapper {

    void insertBatch(@Param("orderDetails") ArrayList<OrderDetail> orderDetails);
}
