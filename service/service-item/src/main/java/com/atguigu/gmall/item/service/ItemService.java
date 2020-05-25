package com.atguigu.gmall.item.service;

import java.util.Map;

public interface ItemService {


    /**
     * 需要将数据封装到map中
     * @param skuId
     * @return
     */
    Map<String,Object> getBySkuId(Long skuId);

}
