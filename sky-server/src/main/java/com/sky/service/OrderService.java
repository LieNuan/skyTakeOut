package com.sky.service;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 查询历史订单
     */
    PageResult historyOrders(Integer page, Integer pageSize, Integer status);

    /**
     * 查询订单详情
     */
    OrderVO detail(Long id);

    /**
     * 订单支付，生成微信预支付交易单
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     */
    void paySuccess(String outTradeNo);

    /**
     * 客户催单
     */
    void reminder(Long id);
}
