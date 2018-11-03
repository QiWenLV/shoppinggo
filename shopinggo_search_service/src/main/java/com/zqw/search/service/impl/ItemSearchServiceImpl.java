package com.zqw.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.container.page.Page;
import com.sun.org.apache.xerces.internal.xs.LSInputList;
import com.zqw.pojo.TbItem;
import com.zqw.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();
        //高亮搜索
        map.putAll(searchList(searchMap));
        map.put("categoryList", searchCategoryLsit(searchMap));

        return map;
    }

    private Map searchBrandAndSpecList(String caregory){
        Map map = new HashMap();
        //1.根据商品分类名称去查找模板ID
//        Long itemCat = redisTemplate.boundHashOps("itemCat").get(caregory);
        return map;
    }

    /**
     * 分组查询
     * @param searchMap
     * @return
     */
    private List searchCategoryLsit(Map searchMap){

        List<String> list = new ArrayList<>();
        //
        Query query = new SimpleQuery("*:*");
        //关键字搜索
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //分组查询,按品牌分组
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        org.springframework.data.domain.Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();

        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());
        }
        return list;
    }

    /**
     * 高亮搜索
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        //结果集
        Map map = new HashMap();
        //普通的关键字查询
//        Query query = new SimpleQuery("*:*");
//        //按照搜索关键字，查询item_keywords复制域
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//
//        map.put("rows", page.getContent());

        //高亮的关键字查询
        HighlightQuery query = new SimpleHighlightQuery();

        //高亮域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //后缀
        highlightOptions.setSimplePostfix("</em>");

        query.setHighlightOptions(highlightOptions);

        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //返回一个高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();

        for(HighlightEntry<TbItem> entry : entryList){
            //获取高亮列表(这个集合的大小取决于高亮域的个数)
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();

//            for(HighlightEntry.Highlight h : highlightList){
//                List<String> sns = h.getSnipplets();         //每个域可能有多个值
//                System.out.println(sns);
//            }
            //因为这里只有一个需要高亮的域，而且是单值域，所以用get(0)
            if(highlightList.size() > 0 && highlightList.get(0).getSnipplets().size()>0){
                //获取域对象，修改原有数据，改成高亮数据
                TbItem item = entry.getEntity();
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", page.getContent());
        return map;
    }
}
