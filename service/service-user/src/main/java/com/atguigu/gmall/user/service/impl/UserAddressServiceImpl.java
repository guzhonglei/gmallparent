package com.atguigu.gmall.user.service.impl;


import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService{

    @Autowired
    private UserAddressMapper userAddressMapper;



    /**
     * 根据用户id查询用户的相关信息
     * @param userId
     * @return
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        // select * from user_address where user_id = ?
        QueryWrapper<UserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        return userAddressMapper.selectList(wrapper);
    }
}
