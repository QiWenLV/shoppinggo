package com.zqw.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zqw.order.service.OrderService;
import com.zqw.pay.service.WeixinPayService;
import com.zqw.pojo.TbPayLog;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){

        //获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //获取支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(username);

        //调用微信的支付接口
        if(payLog != null){
            return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        }else {
            return new HashMap<>();
        }


    }


    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
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
                //修改支付状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
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
                break;
            }

        }

        return result;
    }

}
