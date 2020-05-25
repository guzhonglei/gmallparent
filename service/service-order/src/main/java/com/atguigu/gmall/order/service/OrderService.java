package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderService extends IService<OrderInfo> {


    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    Long saveOrderInfo(OrderInfo orderInfo);


    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 比较流水号
     * @param userId 获取缓存中的流水号
     * @param tradeCodeNo   页面传递过来的流水号
     * @return
     */
    boolean checkTradeCode(String userId, String tradeCodeNo);

    /**
     * 删除流水号
     * @param userId
     */
    void deleteTradeNo(String userId);


    /**
     * 根据订单号获取订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(Long orderId);

}