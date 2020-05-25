package com.atguigu.gmall.all.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PaymentController {


    @Autowired
    private OrderFeignClient orderFeignClient;


    @GetMapping("pay.html")
    public String success(HttpServletRequest request, Model model){

        //获取订单id
        String orderId = request.getParameter("orderId");
        //远程调用订单
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.parseLong(orderId));
        //保存作用域
        //model.addAttribute("orderInfo",orderInfo);
        request.setAttribute("orderInfo",orderInfo);

        return "payment/pay";
    }

}
