package com.zqw.cart.service;

import com.zqw.pojogroup.Cart;

import java.util.List;

public interface CartService {


    /**
     * 添加商品到购物车
     * @param list  原有购物车集合
     * @param itemId    商品ID
     * @param num       商品数量
     * @return      新增后的购物车集合
     */
    public List<Cart> addGoodsToCartList(List<Cart> list, Long itemId, Integer num);


    /**
     * 从redis中提取购物车
     * @param username  小key 用户账号
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车列表存入redis
     * @param username  小key 用户账号
     * @param cartList  购物车列表
     */
    public void saveCartListToRedis(String username, List<Cart> cartList);


    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
