package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现UserDetailsService,调用数据库查询账户
 */
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private XcMenuMapper xcMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto=null;
        try {
            authParamsDto=JSON.parseObject(s,AuthParamsDto.class);
        } catch (Exception e) {
            log.error("请求扔在参数不符合,错误异常{}",e.getMessage());
            throw new RuntimeException(e);

        }
        //获取认证类型
        String authType = authParamsDto.getAuthType();
        //拼接bean名字
        String beanName=authType+"_authservice";

        AuthService bean = applicationContext.getBean(beanName, AuthService.class);
        XcUserExt execute = bean.execute(authParamsDto);
        //封装XcUserExt用户信息为UserDetails
        UserDetails build = getUserDetails(execute);

        return build;
    }

    public  UserDetails getUserDetails(XcUserExt execute) {
        String password = execute.getPassword();
        execute.setPassword(null);
        String jsonString = JSON.toJSONString(execute);
        String[] authorities={"test"};
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(execute.getId());
        if (xcMenus.size()>0){
            List<String> strings = new ArrayList<>();
            xcMenus.forEach(xue->{
                strings.add(xue.getCode());
            });

            authorities=strings.toArray(new String[strings.size()]);
        }
        UserDetails build = User.withUsername(jsonString).password(password).authorities(authorities).build();
        return build;
    }
}
