package com.pinyougou.shop.service.impl;

/**
 * DATE:2018/11/30
 * USER:lzlWhite
 */

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.data.annotation.Reference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

/**
 * 自定义认证授权类
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    //@Reference
    private SellerService sellerService;

    //用户在前端页面输入的用户名
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //构造用户角色列表
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //根据用户名到数据库中查询密码
        TbSeller seller = sellerService.findOne(username);
        if (seller != null && "1".equals(seller.getStatus())) {//已经审核过才能登录
            return new User(username, seller.getPassword(), authorities);
        }
        return null;
    }

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }
}
