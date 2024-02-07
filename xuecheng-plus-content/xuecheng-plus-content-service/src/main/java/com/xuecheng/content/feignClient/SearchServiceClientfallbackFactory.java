package com.xuecheng.content.feignClient;

import com.xuecheng.content.model.po.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程调用搜索服务降级策略
 */
@Slf4j
@Component
public class SearchServiceClientfallbackFactory implements FallbackFactory<SearchServiceClient> {


    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("添加课程信息索引发生熔断，索引信息{}，熔断异常{}",courseIndex,throwable.toString());
                return false;
            }
        };
    }
}
