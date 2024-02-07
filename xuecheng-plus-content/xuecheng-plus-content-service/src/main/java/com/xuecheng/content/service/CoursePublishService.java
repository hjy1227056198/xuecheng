package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;

/**
 * 课程预览、发布接口
 */
public interface CoursePublishService{
    /**
     * 获取课程预览信息
     * @param courseId
     * @return
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param companyId
     * @param courseId
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    public void publish(Long companyId,Long courseId);

    /**
     * 课程静态化，生成html文件
     * @param courseId
     * @return
     */
    public File generateCourseHtml(Long courseId);

    /**
     * 上传静态化文件到minio
     * @param courseId
     * @param file
     */
    public void  uploadCourseHtml(Long courseId,File file);
}
