package com.zqw.page.service.impl;

import com.zqw.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

@Component
public class PageDeleteListener implements MessageListener{

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {

        ObjectMessage objectMessage = (ObjectMessage)message;

        try {
            Long[] goodsIds = (Long[])objectMessage.getObject();
            itemPageService.deleteItemHtml(goodsIds);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
