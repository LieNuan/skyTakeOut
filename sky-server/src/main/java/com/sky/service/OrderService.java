package com.sky.service;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
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

    /**
     * 管理端：订单搜索
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 管理端：接单
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 管理端：拒单
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 管理端：取消订单
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 管理端：派送订单
     */
    void delivery(Long id);

    /**
     * 管理端：完成订单
     */
    void complete(Long id);
}
