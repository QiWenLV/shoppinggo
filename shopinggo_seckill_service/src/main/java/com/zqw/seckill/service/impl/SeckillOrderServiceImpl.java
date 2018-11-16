package com.zqw.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.zqw.mapper.TbSeckillGoodsMapper;
import com.zqw.mapper.TbSeckillOrderMapper;
import com.zqw.pojo.TbSeckillGoods;
import com.zqw.pojo.TbSeckillGoodsExample;
import com.zqw.pojo.TbSeckillOrderExample;
import com.zqw.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zqw.pojo.TbSeckillOrder;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import utils.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		TbSeckillOrderExample.Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private IdWorker idWorker;

	/**
	 * 秒杀下单
	 * @param seckillId
	 * @param userId
	 */
	@Override
	public void submitOrder(Long seckillId, String userId) {
        //查询库存
        TbSeckillGoods seckillGoods = (TbSeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(seckillId);

        if (seckillGoods == null) {
            throw  new RuntimeException("商品不存在");
        }
        if(seckillGoods.getStockCount() <= 0){
            System.out.println("库存不足了");
            throw new RuntimeException("商品库存不足");
        }
        //在redis中减库存。
        seckillGoods.setStockCount(seckillGoods.getStockCount() -1);    //减库存
        redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);

        if(seckillGoods.getStockCount() == 0){
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);    //更新数据库
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);   //删除缓存
            System.out.println("抢完了，更新数据库，删除缓存");
        }

        //在redis中添加订单。
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setUserId(userId);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");


        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);

        System.out.println("redis添加秒杀订单成功");
	}

    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        //从缓存中提取出订单信息
        TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);

        if(seckillOrder == null){
            throw new RuntimeException("不存在订单");
        }
        if(seckillOrder.getId().longValue() != orderId.longValue()){
            throw new RuntimeException("订单号不符");
        }

        //修改订单实体的属性
        seckillOrder.setPayTime(new Date());
        seckillOrder.setStatus("1");    //已支付
        seckillOrder.setTransactionId(transactionId);

        //存入数据库
        seckillOrderMapper.insert(seckillOrder);

        //清除缓存的中订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);

    }

    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //查询出缓存中的订单
        TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
        if(seckillOrder != null){
            //删除缓存中的订单
			redisTemplate.boundHashOps("seckillOrder").delete(userId);

			//库存回退，先查出来这个商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods)redisTemplate.boundHashOps("seckillOrder").get(seckillOrder.getSeckillId());
			//如果还有，代表没秒杀完。
			if(seckillGoods != null){
				seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
				redisTemplate.boundHashOps("seckillOrder").put(seckillOrder.getSeckillId(), seckillGoods);
			}else {
				seckillGoods = new TbSeckillGoods();
				//从数据库中查找基本数据
				seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());

				seckillGoods.setStockCount(1);//数量为1
				redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
			}
			System.out.println("订单取消："+orderId);
        }
    }

}
