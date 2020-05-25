package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartService {


    /**
     * 添加购物车 用户Id，商品Id，商品数量。
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(Long skuId, String userId, Integer skuNum);


    /**
     * 通过用户Id 查询购物车列表
     * @param userId
     * @param userTempId
     * @return
     */
    List<CartInfo> getCartList(String userId,String userTempId);


    /**
     * 更新选中状态
     * @param userId
     * @param isChecked
     * @param skuId
     */
    void checkCart(String userId, Integer isChecked, Long skuId);


    /**
     * 删除购物车
     * @param userId
     * @param skuId
     */
    void deleteCart(String userId, Long skuId);


    /**
     * 根据用户Id 查询购物车列表{被选中的商品组成送货清单}
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
