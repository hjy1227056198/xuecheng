package com.xuecheng.content.feignClient;

import com.xuecheng.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 媒资管理服务远程接口
 */
@FeignClient(value = "media-api",configuration = {MultipartSupportConfig.class})
public interface MediaServerClient {

    @RequestMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value= "objectName",required=false) String objectName);

}
