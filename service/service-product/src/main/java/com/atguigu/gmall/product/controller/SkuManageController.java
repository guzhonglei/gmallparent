package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "sku接口")
@RestController
@RequestMapping("admin/product")
public class SkuManageController {

    @Autowired
    private ManageService manageService;


//    Request URL: http://api.gmall.com/admin/product/spuImageList/11
//    Request Method: GET

    @GetMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable Long spuId){
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }


//    Request URL: http://api.gmall.com/admin/product/spuSaleAttrList/11
//    Request Method: GET

    @GetMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId){
        List<SpuSaleAttr> spuSaleAttrsList = manageService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrsList);
    }


//    Request URL: http://api.gmall.com/admin/product/saveSkuInfo
//    Request Method: POST
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }


//    Request URL: http://api.gmall.com/admin/product/list/1/10
//    Request Method: GET
    @GetMapping("list/{page}/{size}")
    public Result skuInfoList(@PathVariable Long page,
                              @PathVariable Long size){
        Page<SkuInfo> pageParam = new Page<>(page,size);
        IPage<SkuInfo> skuInfoIPage = manageService.selectPage(pageParam);
        return Result.ok(skuInfoIPage);
    }


//    Request URL: http://api.gmall.com/admin/product/onSale/15
//    Request Method: GET
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        manageService.onSale(skuId);
        return Result.ok();
    }


//    Request URL: http://api.gmall.com/admin/product/cancelSale/11
//    Request Method: GET
@GetMapping("cancelSale/{skuId}")
public Result cancelSale(@PathVariable Long skuId){
    manageService.cancelSale(skuId);
    return Result.ok();
}


}
