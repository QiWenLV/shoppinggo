package com.zqw.sellergoods.service;


import com.zqw.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

/**
 * 品牌接口
 * @author Administrator
 *
 */
public interface BrandService {

	public List<TbBrand> findAll();

	public TbBrand findOne(Long id);

	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 添加
	 * @param tbBrand
	 */
	public void add(TbBrand tbBrand);

	/**
	 * 修改
	 * @param brand
	 */
	public void update(TbBrand brand);

	/**
	 * 删除
	 * @param ids
	 */
	public void delete(Long[] ids);
}
