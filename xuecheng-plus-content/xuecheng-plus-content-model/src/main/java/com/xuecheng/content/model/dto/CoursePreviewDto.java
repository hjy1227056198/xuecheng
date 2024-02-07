package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import lombok.Data;

import java.util.List;

/**
 * 课程预览数据模型
 */
@Data
public class CoursePreviewDto{

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachplanDto> teachplans;

//    //课程老师
//    List<CourseTeacher> courseTeachers;
}
