package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 课程教师信息编辑业务接口
 */
public interface CourseTeacherService {
    /**
     * 查询课程教师
     * @param id 教师id
     * @return
     */

    List<CourseTeacher> listTeacher(Long id);

    /**
     * 添加、修改课程教师
     * @param saveCourseTeacherDto
     * @return
     */
    CourseTeacher saveTeacher(Long company_id,SaveCourseTeacherDto saveCourseTeacherDto);




    /**
     * 删除课程教师
     * @param courseId
     * @param id
     */
    void deleteCourseTeacher(Long course_Id, Long courseId,  Long id);

}
