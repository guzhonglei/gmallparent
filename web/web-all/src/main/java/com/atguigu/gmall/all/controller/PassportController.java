package com.atguigu.gmall.all.controller;


import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PassportController {

    @GetMapping("login.html")
    public String login(HttpServletRequest request){

        //这里必须是，从哪里点击，登录成功后，就返回到哪里
        String originUrl = request.getParameter("originUrl");
        System.out.println(originUrl);
        //需要存储originUrl，因为前台需要跳转
        request.setAttribute("originUrl",originUrl);
        //返回到登录页面
        return "login";
    }


}
