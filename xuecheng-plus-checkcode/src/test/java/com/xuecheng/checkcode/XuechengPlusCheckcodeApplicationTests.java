package com.xuecheng.checkcode;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
class XuechengPlusCheckcodeApplicationTests {

    @Test
    void contextLoads() {
        sendEmail();
    }

    /**
     * 方式1：发送QQ邮件
     */
    public void sendEmail() {
        HtmlEmail send = new HtmlEmail();//创建一个HtmlEmail实例对象
        // 获取随机验证码
        String resultCode = random1(4);
        try {
            send.setHostName("smtp.qq.com");
            send.setAuthentication("1227056198@qq.com", "zmggwpukwqixbacj"); //第一个参数是发送者的QQEamil邮箱   第二个参数是刚刚获取的授权码

            send.setFrom("1227056198@qq.com", "orison有限公司");//发送人的邮箱为自己的，用户名可以随便填  记得是自己的邮箱不是qq
//			send.setSmtpPort(465); 	//端口号 可以不开
            send.setSSLOnConnect(true); //开启SSL加密
            send.setCharset("utf-8");
            send.addTo("2559390253@qq.com");  //设置收件人    email为你要发送给谁的邮箱账户
            send.setSubject("测试测试"); //邮箱标题
            send.setMsg("HelloWorld!<font color='red'>您的验证码:</font>   " + resultCode + " ，五分钟后失效"); //Eamil发送的内容
            send.send();  //发送
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }

    //生成验证码
    public static String random1(int length){
        String code = "";
        Random rd=new Random();
        for (int i = 0; i < length; i++) {
            int r = rd.nextInt(10); //每次随机出一个数字（0-9）
            code = code + r;  //把每次随机出的数字拼在一起
        }
        System.out.println(code);
        return code;
    }
}
