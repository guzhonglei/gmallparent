package com.atguigu.gmall.order.controller;


import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/order")
public class OrderApiController {


    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderService orderService;




    /**
     * 确认订单
     * auth/trade 用户走这个控制器,那么必须登录?
     * @param request
     * @return
     */
    @GetMapping("auth/trade")
    public Result<Map<String,Object>> trade(HttpServletRequest request){
        //先获取到用户id
        String userId = AuthContextHolder.getUserId(request);
        //获取用户的地址列表
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        //获取送货清单
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        //一个订单中，有多个小的订单，集合为一个
        List<OrderDetail> orderDetailList = new ArrayList<>();
        // 渲染送货清单
        // 先得到用户想要购买的商品！
        int totalNum = 0;
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            //记录件数 让每个商品的skuNum相加即可
            totalNum+=cartInfo.getSkuNum();
            // 将每个订单明细添加到当前的集合中
            orderDetailList.add(orderDetail);
        }
        // 算出当前订单的总金额。
        OrderInfo orderInfo = new OrderInfo();
        //将订单明细赋值给orderInfo
        orderInfo.setOrderDetailList(orderDetailList);
        //计算总金额
        orderInfo.sumTotalAmount();
        // 将数据封装到map集合中。
        Map<String,Object> map = new HashMap<>();
        // 保存总金额。通过页面trade.html 可以找到页面对应存储的key{totalAmount}
        map.put("totalAmount",orderInfo.getTotalAmount());
        // 保存userAddressList
        map.put("userAddressList",userAddressList);
        // 保存totalNum
        // 那集合长度跟商品数就不一定相等了啊,
        //map.put("totalNum",orderDetailList.size()); 以spu为总数
        // 以sku的件数为总数
        map.put("totalNum",totalNum);
        // 保存detailArrayList
        map.put("detailArrayList",orderDetailList);
        // 返回数据集合
        return Result.ok(map);
    }
    //在下订单之前，校验流水号，流水号不能无刷新，重复提交


    /**
     * 提交订单，下订单的控制器 带有 auth 用户必须登录
     * @param orderInfo
     * @param request
     * @return
     */
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request){
        // userId 在控制器能获取到。暂时不用写。
        String userId = AuthContextHolder.getUserId(request);
        //在保存之前将用户Id赋值给orderInfo
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId = orderService.saveOrderInfo(orderInfo);
        //返回数据
        return Result.ok(orderId);
    }


    /**
     * 内部调用获取订单
     * @param orderId
     * @return
     */
    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId){
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return orderInfo;
    }

}
