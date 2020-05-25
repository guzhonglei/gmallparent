package com.atguigu.gmall.gateway.filter;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter {


    @Value("${authUrls.url}")
    private String authUrls;

    //检查路径匹配的对象
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //先获取用户的请求对象
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        //过滤接口，如果内部接口/**/inner/** 不允许外部访问
        if(antPathMatcher.match("/**/inner/**",path)){
            //获取一个响应对象
            ServerHttpResponse response = exchange.getResponse();
            //不能访问
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //获取用户id
        String userId = getUserId(request);
        //获取临时用户Id
        String userTempId = getUserTempId(request);


        //判断/api/*/auth/** 如果是这样的路径那么应该准备登录
        //如何判断是否登录，就用到了用户缓存的userId
        if(antPathMatcher.match("/api/**/auth/**",path)){
            //判断是否登录
            if(StringUtils.isEmpty(userId)){
                //获取一个响应对象
                ServerHttpResponse response = exchange.getResponse();
                //不能访问
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }

        }

        //验证用户请求的资源，是未登录的不允许访问的路径配置在配置文件中
        if(null != authUrls){
            //authUrls=trade.html,myOrder.html,list.html
            //循环判断
            for (String authUrl : authUrls.split(",")) {
                //判断path中是否包含以上的请求资源
                //如果有，但是用户没有登录，提示用户进行登录
                if(path.indexOf(authUrl) != -1 && StringUtils.isEmpty(userId)){
                    //获取一个响应对象
                    ServerHttpResponse response = exchange.getResponse();
                    //赋值一个状态码
                    //303 由于请求对应的资源，存在另一个url，重定向
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    //重定向到登录链接
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://www.gmall.com/login.html?originUrl="+request.getURI());
                    return response.setComplete();
                }
            }
        }
        //上述全部验证通过，需要将userId传递给各个为服务上
        // 如果用户没有登录，那么在添加购物车的时候，
        // 会产生一个临时用户Id，将临时的用户传递传递给各个微服务
        if(!StringUtils.isEmpty(userId)){
            //存储一个userId
            request.mutate().header("userId",userId);
            //将userId传递下去
            return chain.filter(exchange.mutate().request(request).build());
        }

        if(!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)){
            // 传递登录用户Id
            if(!StringUtils.isEmpty(userId)){
                //存储一个userId
                request.mutate().header("userId",userId);
            }
            // 传递登录用户Id
            if(!StringUtils.isEmpty(userTempId)){
                //存储一个userTempId
                request.mutate().header("userTempId",userTempId);
            }
            // 将用户Id传递下去
            return chain.filter(exchange.mutate().request(request).build());
        }

        return chain.filter(exchange);
    }



    /**
     * 获取userId
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        //用户Id 存储在缓存中
        //key=user:login:token value = userId
        //必须先获取token token可能存在header中，也可能存在cookie中
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if(null != tokenList && tokenList.size()>0){
            //这个集合中只有一个key，这个key token ,值对应的也是一个
            token = tokenList.get(0);
        }else{
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            //表示获取的cookie中多个数据
//            List<HttpCookie> cookieList = cookies.get("token");
//            for (HttpCookie httpCookie : cookieList) {
//                String value = httpCookie.getValue();
//                //添加到list集合中
//                List list = new ArrayList();
//                list.add(value);
//            }
            // 根据cookie 中的key 来获取数据
            HttpCookie cookie = cookies.getFirst("token");
            if(null != cookie){
                token = URLDecoder.decode(cookie.getValue());
            }
        }
        if(!StringUtils.isEmpty(token)){
            //token不为空时，才能从缓存中获取数据
            String userKey = "user:login:" + token;
            String userId = (String) redisTemplate.opsForValue().get(userKey);
            return userId;
        }
        return "";
    }


    /**
     * 提示信息
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        //提示信息告述用户，提示信息封装到ResultCodeEnum对象
        //将提示信息封装到result中
        Result<Object> result = Result.build(null, resultCodeEnum);
        //将result转换成字符串
        String resultStr = JSONObject.toJSONString(result);
        // 将resultStr 转换成一个字节数组
        byte[] bytes = resultStr.getBytes(StandardCharsets.UTF_8);
        // 声明一个DataBuffer
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        // 设置信息输入格式
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
        // 将信息输入到页面
        return response.writeWith(Mono.just(wrap));
    }



    /**
     * 在网关获取临时用户Id 用户在添加购物车中，必然的走网关
     * @param request
     * @return
     */
    private String getUserTempId(ServerHttpRequest request){

        String userTempId = "";
        // 获取临时用户Id与获取用户Id 方法一致
        List<String> userTempIdList = request.getHeaders().get("userTempId");
        if(null != userTempIdList){
            userTempId = userTempIdList.get(0);
        }else{
            // 从cookie中获取
            HttpCookie cookie = request.getCookies().getFirst("userTempId");
            if(null != cookie){
                userTempId = cookie.getValue();
            }
        }
        return userTempId;
    }
}
