package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service
public class ItemServiceImpl implements ItemService {


    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ListFeignClient listFeignClient;



    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String,Object> map = new HashMap<>();

        CompletableFuture<SkuInfo> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //skuInfo的商品详情信息
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        CompletableFuture<Void> spuSaleAttrCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            map.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }, threadPoolExecutor);

        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            map.put("price", skuPrice);
        }, threadPoolExecutor);

        CompletableFuture<Void> categoryViewCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            map.put("categoryView", categoryView);
        }, threadPoolExecutor);

        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
            map.put("valuesSkuJson", valuesSkuJson);
        }, threadPoolExecutor);

        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);
        //skuInfo的商品详情信息
//        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
//
//        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
//
//        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
//
//        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId,skuInfo.getSpuId());
//        System.out.println(spuSaleAttrListCheckBySku);
//
//        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
//        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
//
//        map.put("skuInfo",skuInfo);
//        map.put("price",skuPrice);
//        map.put("categoryView",categoryView);
//        map.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
//        map.put("valuesSkuJson",valuesSkuJson);

        CompletableFuture.allOf(skuCompletableFuture,spuSaleAttrCompletableFuture,priceCompletableFuture,
                categoryViewCompletableFuture,valuesSkuJsonCompletableFuture,incrHotScoreCompletableFuture).join();

        return map;
    }


}
