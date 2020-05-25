package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
public class PassportController {


    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 根据用户id查询用户的相关信息
     * 因为在login.html登录方法中，向后台传递的参数时this.user{属于json}
     * 所以这里使用@RequestBody 转化为java对象
     * 编写映射路径
     * @param userInfo
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo){

        //这里暂时不返回到跳转到登录页面时的页面
        //这里的控制器只处理，数据保存，用户信息
        //返回页面时在其他的控制器中处理
        // http://passport.gmall.com/login.html?originUrl=http://item.gmall.com/15.html
        UserInfo info = userService.login(userInfo);
        if(null != info){

            //这里声明一个map集合，用来存储将要返回的数据
            HashMap<String, Object> map = new HashMap<>();

            //根据sso 的分析过程，用户登录之后的信息，应该放入缓存中，
            //这样才能保证每个模块都可以访问到用户的信息。
            //声明一个token
            String token = UUID.randomUUID().toString().replace("-","");
            //由于token需要存储到cookie中，所以需要将token返回去
            map.put("token",token);
            //由于前台需要用户的名称，所以需要将用户名称返回去
            map.put("nickName",info.getNickName());

            //根据sso分析，用户登录后，将登陆后的信息放入缓存中，这样才能使其他模块也能访问到用户信息
            //定义key=user:login:token value = userId
            String userKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            redisTemplate.opsForValue().set(userKey,info.getId().toString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            //将map集合返回去
            return Result.ok(map);
        }else{
            return  Result.fail().message("用户名或密码错误");
        }
    }


    /**
     * 退出登录
     * @param request
     * @return
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){

        //因为缓存中存储用户数据的时候，需要token，所以删除的时候，需要token组成key
        //当登录成功后，token不仅放到了cookie中，同时还放入了header中
        // 这里通过header，获取token
        String token = request.getHeader("token");

        //删除缓存中的数据
        //key=user:login:token value=userId
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + token);
        //同时清空cookie中的数据
        return Result.ok();
    }
}
