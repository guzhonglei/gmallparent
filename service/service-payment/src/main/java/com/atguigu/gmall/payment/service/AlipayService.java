package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

public interface AlipayService {


    //支付接口，根据订单id ，进行支付
    String createaliPay(Long orderId) throws AlipayApiException;

}
