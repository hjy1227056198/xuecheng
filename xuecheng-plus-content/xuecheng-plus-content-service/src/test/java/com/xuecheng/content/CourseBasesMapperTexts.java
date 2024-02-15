package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseBasesMapperTexts {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Test
    void testCourseBaseMapper(){
        CourseBase courseBase = courseBaseMapper.selectById(18);
        Assertions.assertNotNull(courseBase);

    }



    @Test
    void testCourseBaseInfoService() {
        //查询条件
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");
        queryCourseParamsDto.setAuditStatus("202004");
        queryCourseParamsDto.setPublishStatus("203001");

        //分页参数
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);//页码
        pageParams.setPageSize(3L);//每页记录数

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(null,pageParams, queryCourseParamsDto);
        List<CourseBase> items = courseBasePageResult.getItems();
        System.out.println(items);
    }
    @Test
    void queryTreeModes(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeModes(String.valueOf(1));
        System.out.println(courseCategoryTreeDtos);
    }

    @Test
    void selectTreeNodes(){
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachplanDtos.toString());
    }

}
