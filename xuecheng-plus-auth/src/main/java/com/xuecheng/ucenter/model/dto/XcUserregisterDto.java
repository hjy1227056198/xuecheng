package com.xuecheng.ucenter.model.dto;

import lombok.Data;

/**
 * 注册请求接收数据模型类
 */
@Data
public class XcUserregisterDto {
    private String cellphone; //手机号
    private String username; //账号
    private String email; //邮箱
    private String nickname; //昵称
    private String password; //密码
    private String confirmpwd;
    private String checkcodekey;
    private String checkcode;
}
