package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ManageServiceImpl implements ManageService {


    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private  SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private  SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private  SkuImageMapper skuImageMapper;

    @Autowired
    private  SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private  SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;


    @Override
    public List<BaseCategory1> getCategory1() {
        //select * from base_category1
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        //
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_id",category1Id);
        return baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        //
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_id",category2Id);
        return baseCategory3Mapper.selectList(baseCategory3QueryWrapper);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        //判断，用户到底时根据那一层的分类Id查询的！
        //编写一个xml
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        if(baseAttrInfo.getId() != null){
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }else{
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        //修改平台属性
        QueryWrapper<BaseAttrValue> Wrapper = new QueryWrapper<>();
        Wrapper.eq("attr_id",baseAttrInfo.getId());
        baseAttrValueMapper.delete(Wrapper);

        //新增平台属性
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if(attrValueList != null && attrValueList.size()>0){
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        //select * from base_attr_info where id = attrId
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        return baseAttrInfo;
    }

    private List<BaseAttrValue> getAttrValueList(Long attrId){
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id",attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(wrapper);
        return baseAttrValues;
    }


    @Override
    public IPage<SpuInfo> selectPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",spuInfo.getCategory3Id());
        wrapper.orderByDesc("id");
        IPage<SpuInfo> page = spuInfoMapper.selectPage(pageParam, wrapper);
        return page;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    public List<BaseTrademark> getTrademarkList() {
        return null;
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insert(spuInfo);
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList != null && spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if(spuSaleAttrValueList != null && spuSaleAttrValueList.size()>0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList != null && spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id",spuId);
        return spuImageMapper.selectList(wrapper);
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {

        //查询销售属性 和 销售属性值
        //存在不同的表中  使用xml

        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //skuInfo
        skuInfoMapper.insert(skuInfo);
        //sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList != null && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
        //sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
        //skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size()>0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }
    }

    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam) {
        QueryWrapper<SkuInfo> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        IPage<SkuInfo> skuInfoIPage = skuInfoMapper.selectPage(pageParam, wrapper);
        return skuInfoIPage;
    }

    @Override
    public void onSale(Long skuId) {
        //update sku_info set is_sale = 1 where id = skuId;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void cancelSale(Long skuId) {
        //update sku_info set is_sale = 0 where id = skuId;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    @GmallCache(prefix = RedisConst.SKUKEY_PREFIX)
    public SkuInfo getSkuInfo(Long skuId) {
        // 利用redisson获取分布式锁查询数据库
//         return getSkuInfoRedisson(skuId);
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedisson(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            //定义key
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;

            //获取数据
            skuInfo = (SkuInfo)redisTemplate.opsForValue().get(skuKey);
            //要走数据库
            if(null == skuInfo){
                //利用redisson定义分布式锁
                //定义分布式锁的key
                String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
                //准备上锁
                /*
                lock.lock();
                lock.lock(10, TimeUnit.SECONDS);
                boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
                 */
                boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if(flag){
                    try {
                        skuInfo = getSkuInfoDB(skuId);
                        if (null == skuInfo) {
                            SkuInfo skuInfo1 = new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        //将数据库中的放入缓存
                        redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return skuInfo;
                    }catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }else{
                    //未获取到分布式锁，其他线程等待
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //调用方法
                    return getSkuInfoDB(skuId);
                }
            }else {
                //如果用户查询一个在数据库中根本不存在的数据时，我们就存储一个空对象放入缓存
                //实际上我们想要获取的是不是空对象，并且对象的属性也是有值的
                if(null == skuInfo.getId()){
                    return null;
                }
                //走缓冲
                return skuInfo;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //为了防止缓存宕机，可以走数据库
        return getSkuInfoRedis(skuId);
    }

    //获取reids 获取分布式锁
    private SkuInfo getSkuInfoRedis(Long skuId) {
        SkuInfo skuInfo = null;

        try {
            //定义key
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;

            //获取数据
            skuInfo = (SkuInfo)redisTemplate.opsForValue().get(skuKey);

            if(skuInfo == null){
                //走查询数据库，放入缓存,注意添加分布式锁
                //定义分布式锁的key
                String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
                //获取一个随机字符串
                String uuid = UUID.randomUUID().toString();
                //为了防止缓存击穿，执行分布式锁的命令
                Boolean isExist = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                //判断是否添加锁成功
                if(isExist){
                    //获取到分布式锁，走数据数据库查询数据放入缓存
                    System.out.println("获取分布式锁");
                    skuInfo = getSkuInfoDB(skuId);
                    if(skuInfo == null){
                        //为了防止穿透
                        SkuInfo skuInfo1 = new SkuInfo();
                        //放入缓存的超时时间 ， 一天，最好这个空对象的过期时间不要太长
                        redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return skuInfo1;
                    }
                        //重数据库中查询出来的数据不为空
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        //删除锁
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                        redisScript.setResultType(Long.class);
                        redisScript.setScriptText(script);
                        redisTemplate.execute(redisScript, Arrays.asList(lockKey),uuid);
                        return skuInfo;
                }else{
                    //未获取到分布式锁，其他线程等待
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //调用方法
                    return getSkuInfoDB(skuId);
                }
            }else{
                //如果用户查询一个在数据库中根本不存在的数据时，我们就存储一个空对象放入缓存
                //实际上我们想要获取的是不是空对象，并且对象的属性也是有值的
                if(null == skuInfo.getId()){
                    return null;
                }
                //走缓冲
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果缓存宕机了，那么直接访问数据库
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo != null){
            QueryWrapper<SkuImage> wrapper = new QueryWrapper<>();
            wrapper.eq("sku_id",skuId);
            List<SkuImage> skuImageList = skuImageMapper.selectList(wrapper);
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    @Override
    @GmallCache(prefix = "categoryViewByCategory3Id:")
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix = "skuPrice:")
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo != null){
            return skuInfo.getPrice();
        }else{
            return new BigDecimal("0");
        }
    }

    @Override
    @GmallCache(prefix = "spuSaleAttrListCheckBySku:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        //多表关联查询
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);

    }

    @Override
    @GmallCache(prefix = "skuValueIdsMap:")
    public Map getSkuValueIdsMap(Long spuId) {
        HashMap<Object, Object> hasMap = new HashMap<>();
        List<Map> mapList = skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        System.out.println(mapList);
        if(mapList != null &&mapList.size()>0){
            for (Map map : mapList) {
                hasMap.put(map.get("value_ids"),map.get("sku_id"));
            }
        }
        return hasMap;
    }

    @Override
    @GmallCache(prefix = "baseCategoryList:")
    public List<JSONObject> getBaseCategoryList() {

        //1、获取一级、二级、三级分类数据
        List<JSONObject> list = new ArrayList<>();
        //2、组装条件
            //分类id以主外键
        //3、将组装的数据封装到List<JSONObject>
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //按照一级分类id 进行分组
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //定义一个index
        int index = 1;
        //获取一级分类的数据，包含一级分类的id，一级分类的名称
        for (Map.Entry<Long, List<BaseCategoryView>> entry1  : category1Map.entrySet()){
            //获取一级分类id
            Long category1Id = entry1.getKey();
            JSONObject category1 = new JSONObject();
            category1.put("index",index);
            /*这个key 对么？ 商品详情文档*/
            category1.put("categoryId",category1Id);
            //存储categoryName 数据
            List<BaseCategoryView> caregory2List = entry1.getValue();
            String category1Name = caregory2List.get(0).getCategory1Name();
            category1.put("categoryName",category1Name);
            //categoryChild

            //迭代index
            index++;
            //获取二级分类数据
            Map<Long, List<BaseCategoryView>> category2List = caregory2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //准备给二级分类数据赋值，将二级分类数据添加到对应的一级分类categoryChild中
            List<JSONObject> category2Child = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2List.entrySet()) {
                //获取二级分类的id
                Long category2Id = entry2.getKey();
                //申明一个二级分类的数据的对象
                JSONObject category2 = new JSONObject();
                category2.put("categoryId",category2Id);
                //二级分类的名称
                List<BaseCategoryView> category3List = entry2.getValue();
                category2.put("categoryName",category3List.get(0).getCategory2Name());
                //将二级分类数据添加到二级分类集合中
                category2Child.add(category2);

                //二级中含有一个categoryChild 添加三级分类数据

                //获取三级数据
                List<JSONObject> category3Child = new ArrayList<>();
                //循环category3List
                category3List.stream().forEach(category3View->{
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId",category3View.getCategory3Id());
                    category3.put("categoryName",category3View.getCategory3Name());
                    //将三级分类数据添加到三级分类数据集合中
                    category3Child.add(category3);
                });
                //将三级分类数据放入二级分类里面
                category2.put("categoryChild",category3Child);
            }
            //将二级分类数据放入一级分类里面
            category1.put("categoryChild",category2Child);
            //将所有的category1添加到集合中
            list.add(category1);
        }
        return list;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {

        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }

}



