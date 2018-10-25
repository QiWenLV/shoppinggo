package com.zqw.sellergoods.service.impl;

import java.util.Arrays;
import java.util.List;

import com.zqw.mapper.TbBrandMapper;
import com.zqw.pojo.TbBrand;

import com.zqw.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceName = "com.zqw.sellergoods.service.BrandService")
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;

	@Override
	public List<TbBrand> findAll() {

		return brandMapper.selectByExample(null);
//		List<TbBrand> tbBrands = Arrays.asList(new TbBrand(), new TbBrand());
//		return tbBrands;
	}

}
