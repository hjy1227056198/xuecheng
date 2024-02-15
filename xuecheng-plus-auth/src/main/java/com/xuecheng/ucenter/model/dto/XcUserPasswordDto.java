package com.xuecheng.ucenter.model.dto;

import lombok.Data;

/**
 * 用于找回密码接收数据模型类
 */
@Data
public class XcUserPasswordDto {
    private String cellphone;
    private String email;
    private String checkcodekey;
    private String checkcode;
    private String confirmpwd;
    private String password;
}
