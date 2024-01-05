package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程教师编辑接口
 */
@RestController
@Api(value = "课程教师编辑接口",tags = "课程教师编辑接口")
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    /**
     * 查询课程教师
     * @param id
     * @return
     */
    @ApiOperation("查询教师")
    @GetMapping("/courseTeacher/list/{id}")
    public List<CourseTeacher> listTeacher(@PathVariable Long id){
//        Long company_id=1232141425L;
        List<CourseTeacher> courseTeachers = courseTeacherService.listTeacher( id);
        return courseTeachers;

    }

    /**
     * 添加、修改课程教师
     * @param courseTeacher
     * @return
     */
    @ApiOperation("添加教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveTeacher(@RequestBody SaveCourseTeacherDto courseTeacher){
        Long company_id=1232141425L;
            return courseTeacherService.saveTeacher(company_id,courseTeacher);
    }



    /**
     * 删除课程教程
     * @param courseId 课程id
     * @param id 教师id
     */
    @ApiOperation("删除课程教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void deleteCourseTeacher(@PathVariable Long courseId,@PathVariable Long id){
        Long company_id=1232141425L;
            courseTeacherService.deleteCourseTeacher(company_id,courseId,id);
    }

}
