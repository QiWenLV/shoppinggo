package com.zqw.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.zqw.pay.service.WeixinPayService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import utils.HttpClient;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service(timeout = 10000)
public class WeixinPayServiceImpl implements WeixinPayService{


    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;

    /**
     * 创建二维码链接，并返回链接，支付金额，订单号
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {


        System.out.println(appid);
        System.out.println(partner);
        System.out.println(partnerkey);
        System.out.println(notifyurl);

        //产生各种微信API需要的数据。
        //封装参数
        Map<String, String> param = new HashMap<>();

        param.put("appid", appid);
        param.put("mch_id", partner);    //商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机字符串
        param.put("body", "shop_go");       //商品描述
        param.put("out_trade_no", out_trade_no);    //商户订单号
        param.put("total_fee", total_fee);      //标价金额
        param.put("spbill_create_ip", "127.0.0.1");      //终端IP
        param.put("notify_url", notifyurl);  //通知地址
        param.put("trade_type", "NATIVE");      //交易类型


        //调用微信API，获取回调
        try {
            //生成xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //获取结果，转Map
            String xmlResult = httpClient.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("微信返回的结果：" + mapResult);

            HashMap<String, String> map = new HashMap<>();
            map.put("code_url", mapResult.get("code_url")); //二维码链接
            map.put("out_trade_no", out_trade_no);  //商户订单号
            map.put("total_fee", total_fee);    //支付金额

            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {

        //封装参数
        Map<String, String> param = new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);    //商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机字符串
        param.put("out_trade_no", out_trade_no);    //商户订单号

        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("查询结果" + map);

            return map;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map closePay(String out_trade_no) {
        //封装参数
        Map<String, String> param = new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);    //商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机字符串
        param.put("out_trade_no", out_trade_no);    //商户订单号

        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("查询结果" + map);

            return map;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
