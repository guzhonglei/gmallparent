package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {


    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;


    /**
     * 查看购物车
     * @return
     */
    @RequestMapping("cart.html")
    public String cart(){
        return "cart/index";
    }


    /**
     * 添加购物车
     * http://cart.gmall.com/addCart.html?skuId=23&skuNum=1
     * @param skuId
     * @param skuNum
     * @param request
     * @return
     */
    @RequestMapping("addCart.html")
    public String addCart(@RequestParam(name = "skuId") Long skuId,
                          @RequestParam(name = "skuNum") Integer skuNum,
                          HttpServletRequest request){

        // String skuId = request.getParameter("skuId");
        // String userId = AuthContextHolder.getUserId(request);
        cartFeignClient.addToCart(skuId,skuNum);
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 存储skuInfo,skuNum
        //添加成功后，需要跳转到cart/addCart页面，这个页面需要商品的信息和商品数量的信息
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        // 返回添加成功页面
        return "cart/addCart";
    }




}
