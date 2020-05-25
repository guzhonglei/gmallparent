package com.atguigu.gmall.payment.service.impl;


import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;



    //实现类，调用mapper层
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {

        //paymentInfo记录得时当前一个订单得状态
        /*一个订单是不是只有一个交易记录，不能出现这种情况：
        1、当前一个订单，既有支付宝交易记录，也有微信交易记录
        2、当前一个订单，有支付宝支付得两条交易记录，在paymentInfo种不允许存在
        支付宝中有幂等性：确保多个人支付时，只要有一个人支付成功，其他人将支付失败
        无论当前支付多少次，即只能支付成功一次，

        */
        Integer count = paymentInfoMapper.selectCount(new QueryWrapper<PaymentInfo>().eq("order_id", orderInfo.getId()).eq("payment_type", paymentType));
        if(count>0) return;

        //创建一个paymentInfo对象
        PaymentInfo paymentInfo = new PaymentInfo();
        //给paymentInfo赋值 数据来自于orderInfo
        //查询orderInfo数据，{}{}根据订单id获取数据
        Long id = orderInfo.getId();
        paymentInfo.setOrderId(id);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setPaymentType(paymentType);

        paymentInfoMapper.insert(paymentInfo);

    }
}
