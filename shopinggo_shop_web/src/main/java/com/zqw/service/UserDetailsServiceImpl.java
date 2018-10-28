package com.zqw.service;

import com.zqw.pojo.TbSeller;
import com.zqw.sellergoods.service.SellerService;
import com.zqw.shop.controller.SellerController;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    //远程调用，因为不是Controller，不想使用注解注入。
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService){
        this.sellerService = sellerService;
    }


    //参数，用户在页面输入的用户名
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        System.out.println("经过了这里");

        //构建一个角色列表
        List<GrantedAuthority> grantAuths = new ArrayList<>();
        grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //调用Service，获取数据库数据
        TbSeller seller = sellerService.findOne(username);
        if(seller != null){
            //未通过审核的商家也不能登录
            if(seller.getStatus().equals("1")){
                //这里的User信息是数据库正确的用户信息，框架自动去校验
                return new User(username, seller.getPassword(), grantAuths);
            }else {
                return null;
            }
        }else {
            return null;
        }

       //角色列表：指定用户可以访问和不能访问的页面
    }
}
