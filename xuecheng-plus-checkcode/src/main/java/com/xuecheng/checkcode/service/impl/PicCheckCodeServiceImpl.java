package com.xuecheng.checkcode.service.impl;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.utils.EncryptUtil;
import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.send.EmailSend;
import com.xuecheng.checkcode.service.AbstractCheckCodeService;
import com.xuecheng.checkcode.service.CheckCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.M
 * @version 1.0
 * @description 图片验证码生成器
 * @date 2022/9/29 16:16
 */
@Service("PicCheckCodeService")
@Slf4j
public class PicCheckCodeServiceImpl extends AbstractCheckCodeService implements CheckCodeService {


    @Autowired
    private DefaultKaptcha kaptcha;
    @Autowired
    private EmailSend emailSend;
    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name="NumberLetterCheckCodeGenerator")
    @Override
    public void setCheckCodeGenerator(CheckCodeGenerator checkCodeGenerator) {
        this.checkCodeGenerator = checkCodeGenerator;
    }

    @Resource(name="UUIDKeyGenerator")
    @Override
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }


    @Resource(name="RedisCheckCodeStore")
    @Override
    public void setCheckCodeStore(CheckCodeStore checkCodeStore) {
        this.checkCodeStore = checkCodeStore;
    }


    @Override
    public CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto) {
        GenerateResult generate = generate(checkCodeParamsDto, 4, "checkcode:", 300);
        String key = generate.getKey();
        String code = generate.getCode();
        String pic = createPic(code);
        CheckCodeResultDto checkCodeResultDto = new CheckCodeResultDto();
        checkCodeResultDto.setAliasing(pic);
        checkCodeResultDto.setKey(key);
        return checkCodeResultDto;

    }



    private String createPic(String code) {
        // 生成图片验证码
        ByteArrayOutputStream outputStream = null;
        BufferedImage image = kaptcha.createImage(code);

        outputStream = new ByteArrayOutputStream();
        String imgBase64Encoder = null;
        try {
            // 对字节数组Base64编码
            BASE64Encoder base64Encoder = new BASE64Encoder();
            ImageIO.write(image, "png", outputStream);
            imgBase64Encoder = "data:image/png;base64," + EncryptUtil.encodeBase64(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imgBase64Encoder;
    }
    /**
     * 邮箱or手机 验证码接口
     * @param param1
     * @return
     */
    @Override
    public String phoneverify(String param1,int size) {
        //判断是手机还是邮箱
        boolean contains = param1.contains(".");
        if (contains){
            //走发送邮箱
            String code = null;
            try {
                code = emailSend.sendEmail(param1, size);
            } catch (Exception e) {
                log.error("邮箱验证码发送异常，异常信息{}",e.getMessage());
                XueChengPlusException.cast("邮箱发送异常");
            }
            //将验证码存入redis中
            try {
                ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
                valueOperations.set("qqcoed",code,1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("验证码存入redis中异常，异常信息{}",e.getMessage());
                XueChengPlusException.cast("验证码存入redis中失败");
            }
            return code;
        }else {
            //走发送手机验证码
            return null;
        }

    }
}
