package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 课程预览，发布
 */
@Controller
public class CoursePublishController {
    @Autowired
    private CoursePublishService coursePublishService;

    /**
     * 获取课程预览信息
     * @param courseId
     * @return
     */
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        ModelAndView modelAndView = new ModelAndView();
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    /**
     * 提交审核
     * @param courseId
     */
    @ResponseBody
    @ApiOperation("提交审核")
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();

        Long companyId = null;
        if (StringUtils.isNotEmpty(user.getCompanyId())){
            companyId=Long.parseLong(user.getCompanyId());
        }

        coursePublishService.commitAudit(companyId,courseId);
    }

    /**
     * 课程发布
     * @param courseId
     */
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();

        Long companyId = null;
        if (StringUtils.isNotEmpty(user.getCompanyId())){
            companyId=Long.parseLong(user.getCompanyId());
        }
        coursePublishService.publish(companyId,courseId);
    }
}
