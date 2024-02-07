package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.feignClient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CourseIndex;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 课程发布任务类
 */
@Component
@Slf4j
public class CoursePublicTask extends MessageProcessAbstract {
    @Autowired
    private CoursePublishService coursePublishService;
    @Autowired
    private SearchServiceClient searchServiceClient;
    @Autowired
    private CoursePublishMapper coursePublishMapper;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }
    @Override
    public boolean execute(MqMessage mqMessage) {

        //课程id
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //课程静态化上传minio
        generateCourseHtml(mqMessage,courseId);

        //向es写入索引数据
        saveCourseIndex(mqMessage,courseId);

        //向redis写入缓存


        return true;
    }



    /**
     * 向es写入索引数据
     * @param mqMessage
     * @param courseId
     */
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        //任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //任务幂等性处理
        //查询数据库任务阶段状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo>0){
            log.info("课程任务向es写入索引数据已完成，无需处理...");
            return;
        }
        //向es写入课程索引数据
        //查询已经发布课程信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);

        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        //添加索引
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add){
            XueChengPlusException.cast("远程调用搜索服务添加索引失败");
        }
        //任务完成，改写任务状态
        mqMessageService.completedStageTwo(taskId);
    }


    /**
     * 课程静态化，上传minio
     * @param mqMessage
     * @param courseId
     */
    private void generateCourseHtml(MqMessage mqMessage,Long courseId){
        //任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //任务幂等性处理
        //查询数据库任务阶段状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne>0){
            log.info("课程任务静态化已完成，无需处理...");
            return;
        }

        //开始进行课程静态化
        //生成html文件
        File file = coursePublishService.generateCourseHtml(courseId);
        if(file!=null){
            //html文件上传到minio
            coursePublishService.uploadCourseHtml(courseId,file);
        }else {
            XueChengPlusException.cast("生成的静态文件为空");
        }

        //任务完成，改写任务状态
        mqMessageService.completedStageOne(taskId);


    }
}
