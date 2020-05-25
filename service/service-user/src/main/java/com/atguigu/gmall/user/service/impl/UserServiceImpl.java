package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;


    @Override
    public UserInfo login(UserInfo userInfo) {

        //用户名、密码数据应该是唯一的，所以返回的只能是一个对象
        //验证密码的时候，密码是加密的，所以不能直接userInfo.getPasswd()
        //所以需要先获取用户的密码进行加密，然后进行查询
        String passwd = userInfo.getPasswd();
        String newPwd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        QueryWrapper<UserInfo> wrapper = new QueryWrapper();
        wrapper.eq("login_name",userInfo.getLoginName()).eq("passwd",newPwd);
        //查询之后获取的对象
        UserInfo selectOne = userInfoMapper.selectOne(wrapper);
        //判断对象是否为空
        if(null != selectOne){
            //对象不为空，直接返回
            return selectOne;
        }
        //对象不存在，直接返回空值
        return null;
    }
}
