package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品基础属性接口")
@RestController
@RequestMapping("admin/product")
public class BaseManageController {

    @Autowired
    private ManageService manageService;


    /**
     * 查询所有一级分类信息
     * @return
     */
    @GetMapping("getCategory1")
    public Result getCategory1(){
        List<BaseCategory1> category1 = manageService.getCategory1();
        return Result.ok(category1);
    }


    /**
     * 根据一级分类id 查询二级分类的数据
     * @param category1Id
     * @return
     */
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        List<BaseCategory2> category2 = manageService.getCategory2(category1Id);
        return Result.ok(category2);
    }


    /**
     * 根据二级分类id 查询三级分类的数据
     * @param category2Id
     * @return
     */
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        List<BaseCategory3> category3 = manageService.getCategory3(category2Id);
        return Result.ok(category3);
    }


    /**
     * 根据分类Id 获取平台属性数据
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id){
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return  Result.ok(attrInfoList);
    }


    /**
     * 保存平台属性方法
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }


    /**
     *
     * @param attrId
     * @return
     */
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
        BaseAttrInfo baseAttrInfo = manageService.getAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        return Result.ok(attrValueList);
    }


    @GetMapping("{page}/{size}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long size,
                        SpuInfo spuInfo){
        Page<SpuInfo> pageParam = new Page<>(page,size);
        IPage<SpuInfo> spuInfoIPage = manageService.selectPage(pageParam, spuInfo);
        return Result.ok(spuInfoIPage);
    }
}
