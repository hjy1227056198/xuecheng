package com.xuecheng.learning.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 我的课程表service接口实现类
 */
@Service
@Slf4j
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    MyCourseTablesServiceImpl currentProxy;
    /**
     * 添加选课
     * @param userId 用户id
     * @param courseId 课程id
     * @return
     */
    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {

        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        //课程收费标准
        String charge = coursepublish.getCharge();
        //选课记录
        XcChooseCourse chooseCourse = null;
        if("201000".equals(charge)){//课程免费
            //添加免费课程
            chooseCourse  = addFreeCoruse(userId, coursepublish);
            //添加到我的课程表
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);
        }else{
            //添加收费课程
            chooseCourse  = addChargeCoruse(userId, coursepublish);
        }
        //获取学习资格
        XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());
        return xcChooseCourseDto;
    }
    /**
     * 判断学习资格
     * @param userId
     * @param courseId
     * @return
     */
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables==null){
            XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
            //没有选课或选课后没有支付
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        //是否过期,true过期，false未过期
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if(!isExpires){
            //正常学习
            xcCourseTablesDto.setLearnStatus("702001");
            return xcCourseTablesDto;

        }else{
            //已过期
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
    }


    /**
     * 添加免费课程,免费课程加入选课记录表、我的课程表
     * @param userId
     * @param coursepublish
     * @return
     */
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {

        //查询选课记录表是否存在免费的且选课成功的订单
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001")//免费课程
                .eq(XcChooseCourse::getStatus, "701001");//选课成功
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size()>0) {
            return xcChooseCourses.get(0);
        }
        //添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if (insert<0){
            XueChengPlusException.cast("添加选课记录失败");
        }

        return xcChooseCourse;
    }

    /**
     * 添加收费课程
     * @param userId
     * @param coursepublish
     * @return
     */
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){

        //查询是否存在待交易记录
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(XcChooseCourse::getCourseId,coursepublish.getId())//课程id
                .eq(XcChooseCourse::getUserId,userId)//用户id
                .eq(XcChooseCourse::getOrderType,"700002")//收费订单
                .eq(XcChooseCourse::getStatus,"701002");//待支付
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses!=null && xcChooseCourses.size()>0){
            return xcChooseCourses.get(0);
        }


        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");//待支付

        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if (insert<0){
            XueChengPlusException.cast("添加收费课程记录失败");
        }

        return xcChooseCourse;
    }

    /**
     * 添加到我的课程表
     * @param xcChooseCourse
     * @return
     */
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){
        //选课记录完成且未过期可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)){
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTables!=null){
            return xcCourseTables;
        }
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        int insert = xcCourseTablesMapper.insert(xcCourseTablesNew);
        if (insert<0){
            XueChengPlusException.cast("添加我的课程记录失败");
        }

        return xcCourseTablesNew;
    }

    /**
     * 根据课程和用户查询我的课程表中某一门课程
     * @param userId
     * @param courseId
     * @return
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;

    }


    /**
     * 保存选课为成功状态,并添加到我的课程表
     * @param choosecourseId
     * @return
     */
    @Override
    @Transactional
    public boolean saveChooseCourseStauts(String choosecourseId) {

        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(choosecourseId);
        if (xcChooseCourse==null){
            log.error("接收到课程购买信息，根据选课id在数据库查询不到，选课id：{}",choosecourseId);
            return false;
        }
        //选课状态
        String status = xcChooseCourse.getStatus();
        //判断状态是否未支付
        if ("701002".equals(status)){
            //更新选课记录状态为成功
            xcChooseCourse.setStatus("701001");
            int i = xcChooseCourseMapper.updateById(xcChooseCourse);
            if (i<0){
                log.error("更新选课记录状态为成功失败，选课id{}",xcChooseCourse.getId());
               XueChengPlusException.cast("更新选课记录状态为成功失败");
            }
            //向我的课程表插入记录
            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);
            return true;
        }
        return false;
    }
    /**
     * 我的课程表
     * @param params
     * @return
     */
    @Override
    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params) {
        //页码
        long pageNo = params.getPage();
        //每页记录数,固定为4
        long pageSize = 4;
        //分页条件
        Page<XcCourseTables> page = new Page<>(pageNo, pageSize);
        //根据用户id查询
        String userId = params.getUserId();
        LambdaQueryWrapper<XcCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId);

        //分页查询
        Page<XcCourseTables> pageResult = xcCourseTablesMapper.selectPage(page, lambdaQueryWrapper);
        List<XcCourseTables> records = pageResult.getRecords();
        //记录总数
        long total = pageResult.getTotal();
        PageResult<XcCourseTables> courseTablesResult = new PageResult<>(records, total, pageNo, pageSize);
        return courseTablesResult;
    }
}
