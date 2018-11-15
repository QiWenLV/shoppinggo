package com.zqw.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zqw.pojo.TbBrand;
import com.zqw.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		System.out.println("成功");
		List<TbBrand> lt = brandService.findAll();
		System.out.println(lt);

		return lt;
	}

	@RequestMapping("/findOne")
	public TbBrand findOne(Long id){
		return brandService.findOne(id);
	}

	@RequestMapping("/findPage")
	public PageResult findPage(int page, int size){
		return brandService.findPage(page, size);
	}

	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand){

		try {
			brandService.add(brand);
			return new Result(true, "增加成功");
		}catch (Exception e){
			return new Result(false, "增加失败");
		}
	}

	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand){

		try {
			brandService.update(brand);
			return new Result(true, "修改成功");
		}catch (Exception e){
			return new Result(false, "修改失败");
		}
	}

	@RequestMapping("/delete")
	public Result delete(Long[] ids){

		try {
			brandService.delete(ids);
			return new Result(true, "删除成功");
		}catch (Exception e){
			return new Result(false, "删除失败");
		}
	}



	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand, int page, int size){
		return brandService.findPage(brand, page, size);
	}

	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList() {
        System.out.println("------");
        List<Map> maps = brandService.selectOptionList();
        System.out.println(maps);
        return maps;
	}


}
