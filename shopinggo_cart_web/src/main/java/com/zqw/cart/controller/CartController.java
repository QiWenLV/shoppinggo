package com.zqw.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.zqw.cart.service.CartService;
import com.zqw.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utils.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num){

        //当前登录人账号
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        try {

            //提取现有购物车信息
            List<Cart> cartList = findCartList();

            //调用服务方法，操作购物车(不论cookie还是redis，添加购物车的逻辑是一样的)
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);


            if(name.equals("anonymousUser")){   //没有登录
                //将新的购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);
                System.out.println(cartListString);
                CookieUtil.setCookie(request, response, "cartList", cartListString,3600*24 ,"UTF-8");
                System.out.println("向cookie存储购物车");
            }else{  //如果登录了
                //将新的购物车存入redis
                cartService.saveCartListToRedis(name, cartList);
            }

            return new Result(true, "存入购物车");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, "添加购物车失败");
        }


    }


    //从cookie中提取购物车
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){

        //当前登录人账号
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人账号："+name);

        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if(cartListString == null || cartListString.equals("")){
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);


        if(name.equals("anonymousUser")){//如果未登录

            //从cookie中获取购物车
            System.out.println("从cookie中获取购物车");
            return cartList_cookie;
        }else {
            //已经登录，从redis中获取数据
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);

            if(cartList_cookie.size() > 0){ //当cookie中存在数据时，合并，清空
                //合并购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                //将合并后的购物车存入redis中
                cartService.saveCartListToRedis(name, cartList);
                //清空cookie
                CookieUtil.deleteCookie(request, response, "cartList");

                return cartList;
            }
            return cartList_redis;


        }


    }
}
