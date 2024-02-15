package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.dto.XcUserPasswordDto;
import com.xuecheng.ucenter.model.po.XcUser;

/**
 * 统一认证的接口
 */
public interface AuthService {
    /**
     * 认证方法
     * @param authParamsDto
     * @return
     */
    XcUserExt execute(AuthParamsDto authParamsDto);

}
