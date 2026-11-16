package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders order);

    /**
     * 分页查询订单
     */
    List<Orders> list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单号查询订单
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 根据id查询订单
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 修改订单信息
     */
    void update(Orders orders);

    //查询超时订单
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> findByStatusAndTimeLT(Integer status, LocalDateTime time);


    Double sumByMap(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end,
                    @Param("status") Integer status);

    Integer countByMap(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end,
                       @Param("status") Integer status);

    //查询指定时间区间内销量排名前10的商品
    List<Map<String, Object>> getSalesTop10(@Param("begin") LocalDateTime begin,
                                           @Param("end") LocalDateTime end,
                                           @Param("status") Integer status);

}
