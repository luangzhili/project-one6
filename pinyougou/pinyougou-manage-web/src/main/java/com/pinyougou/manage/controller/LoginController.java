package com.pinyougou.manage.controller;

/**
 * DATE:2018/11/29
 * USER:lzlWhite
 */

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 *登录页面
 */
@RequestMapping("/login")
@RestController
public class LoginController {
    /**
     * 从security 认证信息中获取当前登录人信息
     * @return 当前登录人
     */
    @GetMapping("/getUsername")
    public Map<String,Object> getUsername(){
        Map<String,Object> map = new HashMap<>();
        //从security中获取登录用户名
        String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username",username);
        return map;
    }
}
