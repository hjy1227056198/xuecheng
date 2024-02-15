package com.xuecheng.ucenter.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.XcUserPasswordDto;
import com.xuecheng.ucenter.model.dto.XcUserregisterDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AuthUserServiceImpl implements AuthUserService {
    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CheckCodeClient checkCodeClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private XcUserRoleMapper xcUserRoleMapper;
    @Autowired
    private AuthUserServiceImpl authUserService;
    /**
     * 找回密码
     * @param xcUser
     */
    @Override
    public void findpassword(XcUserPasswordDto xcUser) {
        //判断输入的是手机号码还是电子邮箱
        if (StringUtils.isBlank(xcUser.getEmail()) || (StringUtils.isNotBlank(xcUser.getEmail()) && StringUtils.isNotBlank(xcUser.getCellphone()))){
            //输入的是手机号码
        }else {
            //输入的是电子邮箱
            //查询数据库是否存在该账户
            XcUser xcUser1 = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getEmail, xcUser.getEmail()));
            if (xcUser1==null){
                XueChengPlusException.cast("该账户不存在");
            }
            if (xcUser.getCheckcodekey()==null){
                XueChengPlusException.cast("请输入验证码");
            }
            //校验验证码是否正确
            String code = null;
            try {
                ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
                code = valueOperations.get("qqcoed");
            } catch (Exception e) {
                log.error("获取redis验证码失败，失败异常信息{}",e.getMessage());
            }
            if (!code.equals(xcUser.getCheckcode())){
                XueChengPlusException.cast("验证码错误");
            }
            //判断两次密码是否输入相同
            if (!xcUser.getPassword().equals(xcUser.getConfirmpwd())){
                XueChengPlusException.cast("输入的密码不相同");
            }
            //修改密码
            //输入的密码进行加密
            String encode = passwordEncoder.encode(xcUser.getPassword());
            xcUser1.setPassword(encode);
            xcUserMapper.updateById(xcUser1);

        }

    }
    /**
     * 注册账号接口
     * @param xcUserregisterDto
     */
    @Override
    public void register(XcUserregisterDto xcUserregisterDto) {
        //todo:手机号验证码未开发，这里的手机号是邮箱
        if (xcUserregisterDto.getUsername()==null){
            XueChengPlusException.cast("请输入账号");
        }
        //校验验证码是否正确

        //判断两次密码是否输入相同
        if (!xcUserregisterDto.getPassword().equals(xcUserregisterDto.getConfirmpwd())){
            XueChengPlusException.cast("输入的密码不相同");
        }
        //todo:由于手机接收验证码未开发，那邮政接收验证码
        //校验用户是否存在
        XcUser xcUser1 = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getEmail, xcUserregisterDto.getEmail()));
        if (xcUser1!=null){
            XueChengPlusException.cast("该账号已经存在");
        }

        String code = null;
        try {
            ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
            code = valueOperations.get("qqcoed");
        } catch (Exception e) {
            log.error("获取redis验证码失败，失败异常信息{}",e.getMessage());
        }
        if (!code.equals(xcUserregisterDto.getCheckcode())){
            XueChengPlusException.cast("验证码错误");
        }
        //向用户表、用户角色关系表添加数据。
        authUserService.saveXcUser(xcUserregisterDto);
    }

    /**
     * 向用户表、用户角色关系表添加数据
     * @param xcUserregisterDto
     */
    @Transactional
    public void saveXcUser(XcUserregisterDto xcUserregisterDto){
        //保存数据到用户表
        XcUser xcUser = new XcUser();
        xcUser.setId(UUID.randomUUID().toString());
        xcUser.setCellphone(xcUserregisterDto.getCellphone());
        xcUser.setEmail(xcUserregisterDto.getEmail());
        xcUser.setUsername(xcUserregisterDto.getUsername());
        xcUser.setPassword(passwordEncoder.encode(xcUserregisterDto.getPassword()));
        xcUser.setName(xcUserregisterDto.getNickname());
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);
        //保存数据到用户、角色关系表
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(xcUser.getId());
        xcUserRole.setUserId(xcUser.getId());
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);
    }

}
