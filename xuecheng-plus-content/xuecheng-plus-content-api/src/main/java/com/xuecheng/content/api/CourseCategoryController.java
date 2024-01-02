package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *    课程分类接口
 * @author itcast
 */
@RestController
@Api(value = "课程分类接口",tags = "课程分类接口")
public class CourseCategoryController {
    @Autowired
    private CourseCategoryService courseCategoryService;

    /**
     * 课程分类查询接口
     * @return
     */
    @GetMapping("course-category/tree-nodes")
    @ApiOperation("课程分类接口")
    public List<CourseCategoryTreeDto> queryTreeNodes(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeModes("1");

        return courseCategoryTreeDtos;
    }


}
