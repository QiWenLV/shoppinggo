package com.zqw.pay.service;

import java.util.Map;

public interface WeixinPayService {

    /**
     * 创建一个微信支付链接
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee);


    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);
}
