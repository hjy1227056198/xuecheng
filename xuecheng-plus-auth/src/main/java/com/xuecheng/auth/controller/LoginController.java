package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.XcUserPasswordDto;
import com.xuecheng.ucenter.model.dto.XcUserregisterDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.AuthUserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Mr.M
 * @version 1.0
 * @description 测试controller
 * @date 2022/9/27 17:25
 */
@Slf4j
@RestController
public class LoginController {

    @Autowired
    XcUserMapper userMapper;

    @Autowired
    private AuthUserService authUserService;
    @RequestMapping("/login-success")
    public String loginSuccess() {

        return "登录成功";
    }


    @RequestMapping("/user/{id}")
    public XcUser getuser(@PathVariable("id") String id) {
        XcUser xcUser = userMapper.selectById(id);
        return xcUser;
    }

    @RequestMapping("/r/r1")
    public String r1() {
        return "访问r1资源";
    }

    @RequestMapping("/r/r2")
    public String r2() {
        return "访问r2资源";
    }

    /**
     * 找回密码
     * @param xcUser
     */
    @ApiOperation("找回密码")
    @PostMapping("/findpassword")
    public void findpassword(@RequestBody XcUserPasswordDto xcUser){
        authUserService.findpassword(xcUser);
    }

    /**
     * 注册账号接口
     * @param xcUserregisterDto
     */
    @PostMapping("/register")
    @ApiOperation("注册接口")
    public void register(@RequestBody XcUserregisterDto xcUserregisterDto){
        authUserService.register(xcUserregisterDto);


    }
}
