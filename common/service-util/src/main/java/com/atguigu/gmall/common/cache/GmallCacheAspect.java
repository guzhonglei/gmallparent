package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    //引入redis和redissionClient
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point){
        Object result = null;
        Object[] args = point.getArgs();
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
        String prefix = gmallCache.prefix();
        String key = prefix + Arrays.asList(args).toString();
        result = cacheHit(key,methodSignature);
        if(result != null){
            return result;
        }
        //缓存没有数据
        //上锁 分布式锁
        RLock lock = redissonClient.getLock(key + "lock");
        try {
            boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
            if(res){
                result = point.proceed(point.getArgs());
                if(null == result){
                    Object o = new Object();
                    redisTemplate.opsForValue().set(key,JSONObject.toJSONString(o),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                    Class returnType = methodSignature.getReturnType();
                    //现在需要将cache转换成方法的返回值类型即可
                    //return JSONObject.parseObject((byte[]) o,returnType);
                    return o;
                }
                redisTemplate.opsForValue().set(key,JSONObject.toJSONString(result),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                return result;
            }else{
                Thread.sleep(1000);
                return cacheHit(key,methodSignature);
            }
        }catch (Exception e){

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }finally {
            //解锁
            lock.unlock();
        }
        return result;
    }

    //从缓存中获取数据
    private Object cacheHit(String key,MethodSignature methodSignature) {
        String cache = (String) redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(cache)){
            //字符串是项目中的那种数据类型
            //方法的返回值类型是什么，缓存的类型就是什么
            Class returnType = methodSignature.getReturnType();
            //现在需要将cache转换成方法的返回值类型即可
            return JSONObject.parseObject(cache,returnType);
        }
        return null;
    }
}
