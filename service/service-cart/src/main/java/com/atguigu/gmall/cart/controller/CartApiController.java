package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;


    /**
     * 添加购物车 用户Id，商品Id，商品数量。
     * @param skuId
     * @param skuNum
     * @param request
     * @return
     */
    @PostMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request){
        /// 如何获取用户Id http://cart.gmall.com/addCart.html?skuId=26&skuNum=1
        // 要走网关，在网关中设置了获取登录的用户Id，未登录的临时用户Id
        // 在common-util 中有个工具类AuthContextHolder
        // 表示获取登录的用户Id
        String userId = AuthContextHolder.getUserId(request);
        if(StringUtils.isEmpty(userId)){
            // 未登录获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.addToCart(skuId,userId,skuNum);

        //返回添加成功
        return Result.ok();
    }


    /**
     * 通过用户Id 查询购物车列表
     * @param request
     * @return
     */
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request){
        //获取用户
        String userId = AuthContextHolder.getUserId(request);
        //获取临时用户
        String userTempId = AuthContextHolder.getUserTempId(request);
        //调用service方法
        List<CartInfo> cartList = cartService.getCartList(userId, userTempId);
        //数据返回
        return Result.ok(cartList);
    }


    /**
     * 更新购物车中商品的选中状态
     * /checkCart/' + skuId + '/' + isChecked
     * @param skuId
     * @param isChecked
     * @param request
     * @return
     */
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request){
        // 获取用户Id | 跟单点登录结合在一起。
        String userId = AuthContextHolder.getUserId(request);
        // 表示未登录
        if(StringUtils.isEmpty(userId)){
            // 获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        // 调用服务层方法
        cartService.checkCart(userId,isChecked,skuId);
        return Result.ok();
    }


    /**
     * 删除购物车中的商品
     * url: this.api_name + '/deleteCart/' + skuId,
     * @param skuId
     * @param request
     * @return
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId,
                             HttpServletRequest request){
        //登录未登录都可以删除
        //获取用户
        String userId = AuthContextHolder.getUserId(request);
        if(StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(userId,skuId);
        return Result.ok();
    }


    /**
     * 根据用户Id 查询购物车列表{被选中的商品组成送货清单}
     * @param userId
     * @return
     */
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId){
        return cartService.getCartCheckedList(userId);
    }

}
