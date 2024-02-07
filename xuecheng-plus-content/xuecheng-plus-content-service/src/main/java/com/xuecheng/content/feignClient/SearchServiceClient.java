package com.xuecheng.content.feignClient;

import com.xuecheng.config.MultipartSupportConfig;
import com.xuecheng.content.model.po.CourseIndex;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 搜索服务远程调用接口
 */
@FeignClient(value = "search",fallbackFactory =SearchServiceClientfallbackFactory.class )
public interface SearchServiceClient {

    /**
     * 添加课程索引
     * @param courseIndex
     * @return
     */
    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
