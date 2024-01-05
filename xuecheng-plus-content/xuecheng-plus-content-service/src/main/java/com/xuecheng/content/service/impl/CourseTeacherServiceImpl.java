package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.SaveCourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程教师信息编辑接口实现类
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * 查询课程教师
     * @param id 教师id
     * @return
     */
    @Override
    public List<CourseTeacher> listTeacher(Long id) {


        //构建查询条件，根据课程id批量查询
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId,id);

        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(wrapper);

        if (courseTeachers==null){
            return new ArrayList<>();
        }



        return courseTeachers;
    }

    /**
     * 添加、修改课程教师
     * @param saveCourseTeacherDto
     * @return
     */
    @Override
    public CourseTeacher saveTeacher(Long company_id,SaveCourseTeacherDto saveCourseTeacherDto) {

        if (saveCourseTeacherDto.getId()==null){
            saveCourseTeacherDto.setCreateDate(LocalDateTime.now());
            //验证是否为本机构操作
            CourseBase courseBase = courseBaseMapper.selectById(saveCourseTeacherDto.getCourseId());
            if (!courseBase.getCompanyId().equals(company_id) || courseBase==null){
                XueChengPlusException.cast("只允许向机构自己的课程中添加老师");
            }
            CourseTeacher courseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(saveCourseTeacherDto,courseTeacher);
            //插入数据
            courseTeacherMapper.insert(courseTeacher);
            //返回数据
            CourseTeacher courseTeacher1 = courseTeacherMapper.selectById(courseTeacher.getId());
            return courseTeacher1;
        }else {
            //验证是否为本机构操作
            CourseBase courseBase = courseBaseMapper.selectById(saveCourseTeacherDto.getCourseId());
            if (!courseBase.getCompanyId().equals(company_id) || courseBase==null){
                XueChengPlusException.cast("只允许向机构自己的课程中修改老师");
            }
            CourseTeacher courseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(saveCourseTeacherDto,courseTeacher);
            courseTeacherMapper.updateById(courseTeacher);

            return courseTeacher;
        }

    }



    /**
     * 删除课程教师
     * @param courseId
     * @param id
     */
    @Override
    public void deleteCourseTeacher(Long course_Id,Long courseId, Long id) {
        //验证是否为本机构操作
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!courseBase.getCompanyId().equals(course_Id) || courseBase==null){
            XueChengPlusException.cast("只允许向机构自己的课程中删除老师");
        }
        //构建删除条件
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getId,id)
                .eq(CourseTeacher::getCourseId,courseId);
        //删除数据
        courseTeacherMapper.delete(wrapper);
    }
}
