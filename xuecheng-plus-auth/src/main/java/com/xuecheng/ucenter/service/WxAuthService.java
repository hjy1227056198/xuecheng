package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

import java.util.Map;

/**
 * 微信认证接口
 */
public interface WxAuthService {
    /**
     * 申请令牌，携带令牌获取用户信息，保存到数据库
     * @param code 授权码
     * @return
     */
    XcUser wxAuth(String code);



}
