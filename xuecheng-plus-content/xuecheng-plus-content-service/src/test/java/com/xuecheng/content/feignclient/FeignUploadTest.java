package com.xuecheng.content.feignclient;

import com.xuecheng.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServerClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 测试使用feign远程上传文件
 */
@SpringBootTest
public class FeignUploadTest {

  @Autowired
    private MediaServerClient mediaServerClient;
  @Test
  void test(){
      MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("F:\\学成在线项目—资料\\117.html"));
      mediaServerClient.upload(multipartFile,"course/test.html");
  }

}
