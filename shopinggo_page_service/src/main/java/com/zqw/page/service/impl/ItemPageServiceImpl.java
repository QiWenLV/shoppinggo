package com.zqw.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zqw.mapper.TbGoodsDescMapper;
import com.zqw.mapper.TbGoodsMapper;
import com.zqw.mapper.TbItemCatMapper;
import com.zqw.mapper.TbItemMapper;
import com.zqw.page.service.ItemPageService;
import com.zqw.pojo.TbGoods;
import com.zqw.pojo.TbGoodsDesc;
import com.zqw.pojo.TbItem;
import com.zqw.pojo.TbItemExample;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.Configuration;

import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {


    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    //静态页保存路径
    @Value("${pageDir}")
    private String pageDir;

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;


    @Override
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();

        try {
            Template template = configuration.getTemplate("item.ftl");

            //创建数据模型(SPU)
            Map dataModel = new HashMap<>();
            //读出商品主表和扩展表的数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            //读取商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);
            //读取SKU信息
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);    //按SPU的id查询对应的SKU
            criteria.andStatusEqualTo("1");   //审核通过的商品

            //按默认字段进行降序排序，目的是为了让第一天SKU为默认值
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);


            //输出流(确定目录)
            Writer out = new FileWriter(pageDir + goodsId + ".html");

            //进行输出
            template.process(dataModel, out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }
}
