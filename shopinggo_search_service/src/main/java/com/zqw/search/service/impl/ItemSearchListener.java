package com.zqw.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zqw.pojo.TbItem;
import com.zqw.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        TextMessage textMessage= (TextMessage)message;

        try {
            //获取消息内容
            String text = textMessage.getText();
            //将JSON转换为List
            List<TbItem> tbItemList = JSON.parseArray(text, TbItem.class);

            //更新索引库
            itemSearchService.importList(tbItemList);

            System.out.println("索引库更新成功");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
