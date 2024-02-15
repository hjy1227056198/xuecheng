package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 课程基本信息管理业务接口
 */
public interface CourseBaseInfoService {

    /**
     * 课程查询分页接口
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */

    PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程信息
     * @param courseId 课程id
     * @param addCourseDto 课程基本信息
     * @return
     */
    CourseBaseInfoDto createCourseBase(Long courseId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程基本信息
     * @param courseId
     * @return
     */
    CourseBaseInfoDto getCourseBaseId(@PathVariable Long courseId);

    /**
     * 修改课程
     * @param editCourseDto
     * @return
     */
    CourseBaseInfoDto updateCourseBase(Long courseId,EditCourseDto editCourseDto);

    /**
     * 删除课程
     * @param id
     */
    void  deleteCourseBase(Long id);
}
