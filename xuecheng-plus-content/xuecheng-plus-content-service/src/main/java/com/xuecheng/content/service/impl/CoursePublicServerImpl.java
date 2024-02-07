package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.sun.org.apache.xpath.internal.operations.Mult;
import com.xuecheng.base.execption.CommonError;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServerClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程预览、发布接口实现类
 */
@Service
@Slf4j
public class CoursePublicServerImpl implements CoursePublishService {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseTeacherService courseTeacherService;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CoursePublishMapper coursePublishMapper;
    @Autowired
    private MqMessageService mqMessageService;
    @Autowired
    private MediaServerClient mediaServerClient;






    /**
     * 获取课程预览信息
     * @param courseId
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseId(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree= teachplanService.findTeachplanTree(courseId);
        //课程老师
//        List<CourseTeacher> courseTeachers = courseTeacherService.listTeacher(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
//        coursePreviewDto.setCourseTeachers(courseTeachers);
        return coursePreviewDto;
    }

    /**
     * 提交审核
     * @param companyId
     * @param courseId
     */
    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        //课程、营销基本信息
        CourseBaseInfoDto courseBaseId = courseBaseInfoService.getCourseBaseId(courseId);
        //课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        //查询课程是否存在
        if (courseBaseId==null){
            XueChengPlusException.cast("课程不存在");
        }
        //对已提交审核的课程不允许提交审核
        if (courseBaseId.getAuditStatus().equals("202003")){
            XueChengPlusException.cast("课程已提交");
        }

        //本机构只允许提交本机构的课程
        if (courseBaseId.getCompanyId().equals(courseId)){
            XueChengPlusException.cast("只能修改本机构课程");
        }
        //没有上传图片不允许提交审核
        if (StringUtils.isEmpty(courseBaseId.getPic())){
            XueChengPlusException.cast("该课程没有上传图片");
        }
        //没有添加课程计划不允许提交审核
        if (teachplanTree==null || teachplanTree.size()==0){
            XueChengPlusException.cast("没有添加课程计划不允许提交审核");
        }


        //查询课程基本信息、营销信息、计划信息、师资信息插入课程预发布表

        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息
        BeanUtils.copyProperties(courseBaseId,coursePublishPre);
        //课程师资信息
        List<CourseTeacher> courseTeachers = courseTeacherService.listTeacher(courseId);
        String teacherString = JSON.toJSONString(courseTeachers);
        //设置课程师资信息
        coursePublishPre.setTeachers(teacherString);
        //课程营销信息
        CourseMarket courseMarketString = new CourseMarket();
        BeanUtils.copyProperties(courseBaseId,courseMarketString);
        String marketString = JSON.toJSONString(courseMarketString);
        //设置课程营销信息
        coursePublishPre.setMarket(marketString);
        //课程计划信息
        String teachplanString = JSON.toJSONString(teachplanTree);
        //设置课程计划信息
        coursePublishPre.setTeachers(teachplanString);
        //设置提交课程时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //设置课程审核状态已提交
        coursePublishPre.setStatus("202003");
        //插入数据,如果为空则插入，否则更新
        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre1==null){
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本信息表的状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 保存课程发布信息
     * @param courseId
     */
    @Transactional
    public void saveCoursePublic(Long courseId){

        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 保存消息表记录
     * @param courseId
     */
    @Transactional
    public void saveCoursePublishMessage(Long courseId){
        MqMessage coursePublish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (coursePublish==null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }

    }
    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {

        //查询课程发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre==null){
            XueChengPlusException.cast("请先提交课程审核，审核之后才能发布");
        }
        //检验是否本机构操作
        if (!companyId.equals(coursePublishPre.getCompanyId())){
            XueChengPlusException.cast("不允许提交其他机构课程");
        }

        //查询课程审核状态
        if(!"202004".equals(coursePublishPre.getStatus())){
            XueChengPlusException.cast("操作失败，课程审核通过方可发布。");
        }

        //保存课程发布信息
        saveCoursePublic(courseId);
        //保存消息表
        saveCoursePublishMessage(courseId);
        //删除课程预发布表信息记录
        coursePublishPreMapper.deleteById(courseId);
    }
    /**
     * 课程静态化，生成html文件
     * @param courseId
     * @return
     */
    @Override
    public File generateCourseHtml(Long courseId) {

        //静态化文件
        File htmlFile  = null;

        try {
            //指定版本
            Configuration configuration = new Configuration(Configuration.getVersion());
            //拿到classpath路径
            String path = this.getClass().getResource("/").getPath();
            //指定模板目录
            configuration.setDirectoryForTemplateLoading(new File(path+"/templates/"));
            //指定编码
            configuration.setDefaultEncoding("utf-8");

            //得到模板
            Template template = configuration.getTemplate("course_template.ftl");
            //模板数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            HashMap<String, CoursePreviewDto> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put("model",coursePreviewInfo);

            String s = FreeMarkerTemplateUtils.processTemplateIntoString(template, stringObjectHashMap);

            //输入流
            InputStream inputStream = IOUtils.toInputStream(s, "utf-8");
            //输出文件
            htmlFile=File.createTempFile("coursepublish",".html");
            FileOutputStream fileOutputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.getMessage());
            XueChengPlusException.cast("课程静态化异常");
        }

        return htmlFile;
    }
    /**
     * 上传静态化文件到minio
     * @param courseId
     * @param file
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course  = mediaServerClient.upload(multipartFile, "course/" + courseId + ".html");
        if(course==null){
            log.debug("远程调用走降级逻辑得到上传结果为null，课程id{}",courseId);
            XueChengPlusException.cast("上传静态文件异常");
        }
    }
}
