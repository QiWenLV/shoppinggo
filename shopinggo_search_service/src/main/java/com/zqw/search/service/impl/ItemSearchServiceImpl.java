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
        //1.高亮搜索
        map.putAll(searchList(searchMap));
        //2.分类列表
        List<String> categoryLsit = searchCategoryLsit(searchMap);
        map.put("categoryList", categoryLsit);

        //3.品牌列表和规格列表
        String category = (String)searchMap.get("category");
        //当用户选择具体商品分类时，按分类查询规格
        if(!category.equals("")){
            map.putAll(searchBrandAndSpecList(category));
        }else {
            //当用户没有选择品牌时，按第一个商品分类
            if(categoryLsit.size() > 0){
                map.putAll(searchBrandAndSpecList(categoryLsit.get(0)));
            }
        }



        return map;
    }

    private Map searchBrandAndSpecList(String caregory){
        Map map = new HashMap();
        //1.根据商品分类名称去查找模板ID
        Long templateId = (Long)redisTemplate.boundHashOps("itemCat").get(caregory);

        if(templateId != null){
            //2.根据模板ID获取品牌列表
            List brandList = (List)redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);

            //3.根据模板ID获取规格列表
            List specList = (List)redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }



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

        //高亮的选项的初始化
        HighlightQuery query = new SimpleHighlightQuery();
        //高亮域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //后缀
        highlightOptions.setSimplePostfix("</em>");

        query.setHighlightOptions(highlightOptions);
        //1.1关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按照商品分类过滤
        if(!"".equals(searchMap.get("category"))){       //如果选择了商品分类，才进行筛选
            FilterQuery filterQuery = new SimpleFilterQuery();
            //过滤条件
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(categoryCriteria);

            query.addFilterQuery(filterQuery);
        }
        //1.3按照商品品牌过滤
        if(!"".equals(searchMap.get("brand"))){       //如果选择了商品分类，才进行筛选
            FilterQuery filterQuery = new SimpleFilterQuery();
            //过滤条件
            Criteria brandyCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(brandyCriteria);

            query.addFilterQuery(filterQuery);
        }
        //1.4按照规格过滤
        if(searchMap.get("spec") != null){       //如果选择了商品分类，才进行筛选
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (Map.Entry entry : specMap.entrySet()){
                FilterQuery filterQuery = new SimpleFilterQuery();
                //过滤条件
                Criteria specCriteria = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                filterQuery.addCriteria(specCriteria);

                query.addFilterQuery(filterQuery);
            }

        }


        //获取高亮结果集
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
