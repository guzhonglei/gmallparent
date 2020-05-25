package com.atguigu.gmall.order.service.impl;


import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>  implements OrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;


    /**
     * //保存订单
     * @param orderInfo
     * @return
     */
    @Override
    @Transactional
    public Long saveOrderInfo(OrderInfo orderInfo) {

        // 保存orderInfo
        // 缺少部分数据 总金额，userId,订单状态，第三方交易编号，创建订单时间，订单过期时间，进程状态。
        orderInfo.sumTotalAmount();
        // userId 在控制器能获取到。暂时不用写。
        // 订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        // 第三方交易编号 - 给支付宝使用的。能够保证支付的幂等性。
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setTrackingNo(outTradeNo);
        //创建时间
        orderInfo.setCreateTime(new Date());
        //过期时间
        //先获取日历对象
        Calendar calendar = Calendar.getInstance();
        //在日历基础上添加一天
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        // 进程状态 与订单状态有个绑定关系。
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        // 订单的主题描述：获取订单明细中的商品名称，将商品名称拼接在一起。
        // 订单明细：
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        StringBuilder sb = new StringBuilder();
        for (OrderDetail orderDetail : orderDetailList) {
            sb.append(orderDetail.getSkuName() + "");
        }
        // 做个长度处理
        if(sb.toString().length()>100){
            orderInfo.setTradeBody(sb.toString().substring(0,100));
        }else{
            orderInfo.setTradeBody(sb.toString());
        }
        orderInfoMapper.insert(orderInfo);

        for (OrderDetail orderDetail : orderDetailList) {
            //赋值orderId
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }
        //返回订单Id
        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        //获取流水号
        String tradeNo = UUID.randomUUID().toString().replace("-","");
        //将流水号放入缓存
        String tradeNoKey = "user:" + userId + ":tradeCode";
        //使用后String

        return null;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        return false;
    }

    @Override
    public void deleteTradeNo(String userId) {
        //将流水号放入缓存
        String tradeNoKey = "user:" + userId + ":tradeCode";
        //删除流水号
        redisTemplate.delete(userId);
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {

        //查询订单表
        //select * from order_info where id = orderId;
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //查询订单详情表
        //select * from order_detail where order_id = orderId;
        QueryWrapper<OrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }
}
