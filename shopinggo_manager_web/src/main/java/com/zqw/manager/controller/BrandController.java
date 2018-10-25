package com.zqw.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zqw.pojo.TbBrand;
import com.zqw.sellergoods.service.BrandService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/brand")
//@Controller
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
	
}
