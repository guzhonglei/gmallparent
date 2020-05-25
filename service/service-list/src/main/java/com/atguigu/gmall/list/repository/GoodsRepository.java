package com.atguigu.gmall.list.repository;


import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


//可以操作es
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
