package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

public interface UserService {


    /**
     * 登录方法
     * 对密码加密
     * select * from user_info where userName=? and pwd = ?;
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);



}
