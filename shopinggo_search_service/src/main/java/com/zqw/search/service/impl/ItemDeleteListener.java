package com.zqw.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zqw.pojo.TbItem;
import com.zqw.search.service.ItemSearchService;
import org.apache.commons.collections.ArrayStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.Arrays;
import java.util.List;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        ObjectMessage objectMessage= (ObjectMessage)message;

        try {
            //获取消息内容
            Long[] ids = (Long[]) objectMessage.getObject();

            //更新索引库
            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            System.out.println("索引库删除成功："+Arrays.asList(ids));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
