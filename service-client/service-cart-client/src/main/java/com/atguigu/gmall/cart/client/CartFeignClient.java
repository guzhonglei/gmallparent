package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient(name = "service-cart", fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {

    // 远程调用 CartApiController.addToCart();
    @PostMapping("api/cart/addToCart/{skuId}/{skuNum}")
    Result addToCart(@PathVariable Long skuId,
                     @PathVariable Integer skuNum);

    // 通过用户Id 获取送货清单数据
    @GetMapping("api/cart/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable String userId);

}
