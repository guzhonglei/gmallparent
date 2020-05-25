package com.atguigu.gmall.payment.service.impl;


import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.payment.service.AlipayService;
import org.springframework.stereotype.Service;

@Service
public class AlipayServiceImpl implements AlipayService {

    //获取Alipay
    @Override
    public String createaliPay(Long orderId) throws AlipayApiException {
        return null;
    }
}
