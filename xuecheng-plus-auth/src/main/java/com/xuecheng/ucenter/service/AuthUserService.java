package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.XcUserPasswordDto;
import com.xuecheng.ucenter.model.dto.XcUserregisterDto;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthUserService {


    /**
     * 找回密码
     * @param xcUser
     */
    void findpassword(XcUserPasswordDto xcUser);


    /**
     * 注册账号接口
     * @param xcUserregisterDto
     */
    void register( XcUserregisterDto xcUserregisterDto);
}