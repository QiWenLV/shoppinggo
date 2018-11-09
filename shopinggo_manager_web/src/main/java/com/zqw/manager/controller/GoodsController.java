package com.zqw.manager.controller;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.zqw.pojo.TbItem;
import com.zqw.pojogroup.Goods;
import com.zqw.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.zqw.pojo.TbGoods;


import entity.PageResult;
import entity.Result;

import javax.jms.Destination;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination queueSolrDestination;

    @Autowired
    private Destination queueSolrDeleteDestination;

    @Autowired
    private Destination topicPageDestination;
    @Autowired
    private Destination topicPageDeleteDestination;


    /**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}

	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){

		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);

			//从索引库删除
  //          itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

            //发送从索引库删除消息
            jmsTemplate.send(queueSolrDeleteDestination, (session)->{
                return session.createObjectMessage(ids);
            });

            //删除每个服务器上的商品详细页
            jmsTemplate.send(topicPageDeleteDestination, (session)->{
                return session.createObjectMessage(ids);
            });

			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

//
//	@Reference(timeout = 100000)
//	private ItemSearchService itemSearchService;

//	@Reference(timeout = 40000)
//	private ItemPageService itemPageService;


	/**
	 * 修改
	 * @param
	 * @return
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){

		try {

			if("1".equals(status)){	//如果是审核通过
				//得到需要导入的SKU列表
				List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(ids, status);
				System.out.println("前台"+itemList);
				//导入到solr
//				itemSearchService.importList(itemList);
                //转化为JSON，方便传输
                String jsonString = JSON.toJSONString(itemList);
                //发送消息
				jmsTemplate.send(queueSolrDestination, (session)->{
					return session.createTextMessage(jsonString);
				});


				//生成商品详细页
				for(Long goodsId:ids){
					//itemPageService.genItemHtml(goodsId);
                    jmsTemplate.send(topicPageDestination, (session)->{
                        return session.createTextMessage(goodsId+"");
                    });
				}
			}

            goodsService.updateStatus(ids, status);

			return new Result(true, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败");
		}
	}




}
