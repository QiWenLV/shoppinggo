package com.zqw.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zqw.cart.service.CartService;
import com.zqw.mapper.TbGoodsMapper;
import com.zqw.mapper.TbItemMapper;
import com.zqw.pojo.*;
import com.zqw.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {


        //查询商品的详细信息(SKU)
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item == null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态不合法");
        }

        //查询出商品的商家ID和商家名称
        String sellerId = item.getSellerId();  //商家ID


        //是否已经存在商品的店铺
        Cart cart = null;
//        if (cartList != null){
            cart = searchCartBySellerId(cartList, sellerId);
//        }
        if(cart == null){ //不存在
            cart = new Cart();  //为null，可以不用重新创建
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            List<TbOrderItem> orderItems = new ArrayList<>();
            //创建一个商品明细列表，并加入当前商品
            orderItems.add(createOrderItem(num, item));
            cart.setOrderItemList(orderItems);

            //将新创建的店铺放入购物车中
            cartList.add(cart);
        }else { //存在

            TbOrderItem orderItem = searchOrderItmeByItemId(cart.getOrderItemList(), itemId);

            //店铺没有这个商品
            if(orderItem == null){
                //创建商品明细对象，添加到集合中
                cart.getOrderItemList().add(createOrderItem(num, item));

            }else {
                //店铺有这个商品
                orderItem.setNum(orderItem.getNum()+num);   //更改数量
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));  //更改总金额

                //当商品的数量小于等于0
                if(orderItem.getNum() <= 0){
                    cart.getOrderItemList().remove(orderItem);
                }
                //当购车这个店铺的商品为空时，移除这个店铺
                if(cart.getOrderItemList().size() == 0){
                    cartList.remove(cart);
                }
            }
        }

        return cartList;
    }

    //查询是否已经有店铺在购物车了
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId){
        //循环查找
        for (Cart cart : cartList) {
            //已经存在这个店铺
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    //从redis中获取数据
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中获取数据");
        List<Cart> cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(username);

        if(cartList == null){
            cartList = new ArrayList<>();
        }

        return cartList;
    }

    //存数据到redis
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("存数据到redis");
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    //合并两个购物车的数据
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {


        for (Cart cart : cartList2) {

            for(TbOrderItem orderItem : cart.getOrderItemList()){
                addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }


    //根据SKU Id 在店铺商品中查询是已经存在商品
    private TbOrderItem searchOrderItmeByItemId(List<TbOrderItem> orderItemList, Long itemId){

        for (TbOrderItem orderItem : orderItemList) {
            //存在
            if(orderItem.getItemId().longValue() == itemId.longValue()){
                return orderItem;
            }
        }
        //不存在
        return null;
    }


    //创建一个商品明细对象
    private TbOrderItem createOrderItem(Integer num, TbItem item) {

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());    //SPU
        orderItem.setItemId(item.getId());       //SKU
        orderItem.setTitle(item.getTitle());    //商品标题
        orderItem.setNum(num);      //数量
        orderItem.setPicPath(item.getImage());  //图片地址
        orderItem.setPrice(item.getPrice());    //价格
        orderItem.setSellerId(item.getTitle()); //商品名
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num)); //总价
        return orderItem;
    }


}
