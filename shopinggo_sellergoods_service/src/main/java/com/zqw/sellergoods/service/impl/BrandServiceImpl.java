package com.zqw.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zqw.mapper.TbBrandMapper;
import com.zqw.pojo.TbBrand;

import com.zqw.pojo.TbBrandExample;
import com.zqw.sellergoods.service.BrandService;
import entity.PageResult;
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

	@Override
	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {

		PageHelper.startPage(pageNum, pageSize);
		//分页查询所有
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		//返回包装后的结果
		return new PageResult(page.getTotal(), page.getResult());
	}

    @Override
    public void add(TbBrand tbBrand) {
        brandMapper.insert(tbBrand);
    }

	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);
	}

	@Override
	public void delete(Long[] ids) {
		for(Long id : ids){
			brandMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {

		PageHelper.startPage(pageNum, pageSize);
		TbBrandExample example = new TbBrandExample();
		TbBrandExample.Criteria criteria = example.createCriteria();

		if(brand!=null){
		    if(brand.getName()!=null && brand.getName().length() > 0){
                criteria.andNameLike("%" + brand.getName() + "%");
            }
            if(brand.getFirstChar() != null && brand.getFirstChar().length() > 0){
                criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
            }
        }

		//分页查询所有
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
		//返回包装后的结果
		return new PageResult(page.getTotal(), page.getResult());

	}

	@Override
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}
}
