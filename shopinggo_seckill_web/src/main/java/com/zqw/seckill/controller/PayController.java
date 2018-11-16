package com.zqw.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.zqw.pay.service.WeixinPayService;
import com.zqw.pojo.TbPayLog;
import com.zqw.pojo.TbSeckillOrder;
import com.zqw.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative(){

        //获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //获取支付日志
       TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);

        //调用微信的支付接口
        if(seckillOrder != null){
            return weixinPayService.createNative(seckillOrder.getId()+"", (long)(seckillOrder.getMoney().doubleValue()*100)+"");
        }else {
            return new HashMap<>();
        }


    }


    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获取登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Result result = null;
        int x = 0;
        while (true){
            //调用查询
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);

            if(map == null){
                result = new Result(false, "支付错误");
                break;
            }
            //成功
            if("SUCCESS".equals(map.get("trade_state"))){
                result = new Result(true, "支付成功");
                //保存订单
                seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));

                break;
            }

            try{
                Thread.sleep(3000);
            } catch (Exception e){
                e.printStackTrace();
            }
            x++;
            if(x>100){
                //大概5分钟
                result = new Result(false, "二维码超时");
                //关闭微信订单
                Map payResult = weixinPayService.closePay(out_trade_no);
                if(payResult != null && "FAIL".equals(payResult.get("return_code"))){
                    if("ORDERPAID".equals(payResult.get("err_code"))){
                        result = new Result(true, "支付成功");
                        //保存订单
                        seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
                    }
                }
                //删除订单
                if(result.isSuccess()==false){
                    seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;
    }

}
