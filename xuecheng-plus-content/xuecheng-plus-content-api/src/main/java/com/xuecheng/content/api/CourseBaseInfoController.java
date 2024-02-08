package com.xuecheng.content.api;

import com.xuecheng.base.execption.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课程信息编辑接口
 */
@RestController
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
public class CourseBaseInfoController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    /**
     * 课程分页查询接口
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    @ApiOperation("课程分页查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list( PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
        return courseBasePageResult;
    }

    /**
     * 新增课程基本信息
     * @param addCourseDto
     * @return
     */
    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Inster.class) AddCourseDto addCourseDto){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String companyId = user.getCompanyId();
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(Long.valueOf(companyId), addCourseDto);
        return courseBase;
    }

    /**
     * 根据课程id查询课程基本信息
     * @param courseId
     * @return
     */
    @ApiOperation("根据课程id查询课程基本信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseId(@PathVariable Long courseId){
        CourseBaseInfoDto courseBaseId = courseBaseInfoService.getCourseBaseId(courseId);
        return courseBaseId;
    }

    /**
     * 修改课程
     * @param editCourseDto
     * @return
     */
    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
        return courseBaseInfoDto;
    }

    /**
     * 删除课程
     * @param id
     */
    @ApiOperation("删除课程")
    @DeleteMapping("/course/{id}")
    public  void  deleteCourseBase(@PathVariable Long id){
        courseBaseInfoService.deleteCourseBase(id);
    }

}
