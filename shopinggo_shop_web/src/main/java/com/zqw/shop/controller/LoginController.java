package com.zqw.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map name(){
        //通过Security框架来获得登录用户的用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap<>();

        map.put("loginName", name);

        return map;
    }
}
