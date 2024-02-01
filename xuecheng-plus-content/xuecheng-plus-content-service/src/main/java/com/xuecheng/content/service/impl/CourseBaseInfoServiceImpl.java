package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private  CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;



    /**
     * 课程查询分页接口
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> courseBaseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        courseBaseLambdaQueryWrapper
                //构建查询条件，根据课程名称查询
                .like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName())
                //构建查询条件，根据课程审核状态查询
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus())
                //构建查询条件，根据课程发布状态查询
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        //分页对象
        Page<CourseBase> coursePage = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //查询结果
        Page<CourseBase> page=courseBaseMapper.selectPage(coursePage,courseBaseLambdaQueryWrapper);
        //获取数据列表
        List<CourseBase> records = page.getRecords();
        //获取总页数
        long total = page.getTotal();
        //封装结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());

        return courseBasePageResult;
    }
    /**
     * 添加课程信息
     * @param courseId 课程id
     * @param dto 课程基本信息
     * @return
     */
    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long courseId, AddCourseDto dto) {

        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            XueChengPlusException.cast("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            XueChengPlusException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            XueChengPlusException.cast("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            XueChengPlusException.cast("收费规则为空");
        }
        //新增对象
        CourseBase courseBase = new CourseBase();
        //对象拷贝
        BeanUtils.copyProperties(dto,courseBase);
        //设置审核默认状态
        courseBase.setAuditStatus("202002");
        //设置发布默认撞他
        courseBase.setStatus("203001");
        //设置创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //设置课程id
        courseBase.setCompanyId(courseId);
        //插入课程基本信息
        int insert = courseBaseMapper.insert(courseBase);
        if (insert<0){
            XueChengPlusException.cast("课程信息新增失败");
        }
        //新增课程营销对象
        CourseMarket courseMarket = new CourseMarket();
        //数据拷贝
        BeanUtils.copyProperties(dto,courseMarket);
        //设置id
        courseMarket.setId(courseBase.getId());

        //插入课程营销数据
        saveCourseMarket(courseMarket);
        //组装最终数据对象，并设置大小分类名称
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfoDto(courseBase.getId());

        return courseBaseInfoDto;
    }



    //插入课程营销信息
    private int saveCourseMarket(CourseMarket courseMarket){
        //获取收费规则
        String charge = courseMarket.getCharge();
        //判断收费规则是否为空
        if (StringUtils.isBlank(charge)){
            XueChengPlusException.cast("收费规则没有选择");
        }
        //如果选择收费，判断输入的价格不能为空且大于等于0
        if (charge.equals("201001")){
            if (courseMarket.getPrice()==null || courseMarket.getPrice().floatValue()<=0){
                XueChengPlusException.cast("课程收费的价格不能为空且不能小于0");
            }
        }
        //查询课程营销信息
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseMarket.getId());
        //为空，则插入课程营销信息
        if (courseMarket1==null){
            int insert = courseMarketMapper.insert(courseMarket);
            return insert;
        //不为空，则跟新课程营销信息
        }else {
            BeanUtils.copyProperties(courseMarket,courseMarket1);
            courseMarket1.setId(courseMarket.getId());
            int i = courseMarketMapper.updateById(courseMarket1);
            return i;
        }
    }
    //组装最终数据对象，并设置大小分类名称
    public CourseBaseInfoDto getCourseBaseInfoDto(Long courseId){
        //查询课程分类信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase==null){
            return null;
        }
        //查询课程营销数据
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //定义最终返回数据对象
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        //拷贝课程基本信息
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        //拷贝课程营销信息
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        //获取课程大分类对象
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBase.getMt());
        //获取课程小分类对象
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBase.getSt());
        //设置课程大分类名字
        courseBaseInfoDto.setMtName(courseCategory.getName());
        //设置课程小分类名字
        courseBaseInfoDto.setStName(courseCategory1.getName());
        return courseBaseInfoDto;
    }

    /**
     * 根据课程id查询课程基本信息
     * @param courseId
     * @return
     */
    @Override
    public CourseBaseInfoDto getCourseBaseId(Long courseId) {
        //根据课程id查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //根据课程id查询课程营销基本信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //定义最终返回对象
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        //课程基本信息拷贝
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if (courseMarket!=null){
            //课程营销基本信息拷贝
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询课程大分类
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBase.getMt());
        //查询课程小分类
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBase.getSt());
        //设置课程大分类名称
        courseBaseInfoDto.setMtName(courseCategory.getName());
        //设置课程小分类名称
        courseBaseInfoDto.setStName(courseCategory1.getName());


        return courseBaseInfoDto;
    }

    /**
     * 修改课程
     * @param editCourseDto
     * @return
     */
    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long courseId,EditCourseDto editCourseDto) {
        //根据id查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(editCourseDto.getId());
        //校验本机构只能修改本机构的课程
        if (!courseId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        //查询课程基本信息是否存在
        if (courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }
        //新建课程营销对象
        CourseMarket courseMarket = new CourseMarket();
        //设置课程营销对象id
        courseMarket.setId(editCourseDto.getId());
        //拷贝课程基本信息数据
        BeanUtils.copyProperties(editCourseDto,courseBase);
        //拷贝课程营销数据
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        courseBase.setChangeDate(LocalDateTime.now());
        //修改课程基本数据
        courseBaseMapper.updateById(courseBase);
        //检验课程营销收费规则
        if (courseMarket.getCharge().equals("201001")){
            if (courseMarket.getPrice()==null || courseMarket.getPrice().floatValue()<=0){
                XueChengPlusException.cast("课程收费的价格不能为空且不能小于0");
            }
        }
        //根据id查询课程营销数据
        CourseMarket courseMarket1 = courseMarketMapper.selectById(editCourseDto.getId());
        //如果为空则插入，否则修改营销信息
        if (courseMarket1==null){
            int insert = courseMarketMapper.insert(courseMarket);
            if (insert<0){
                XueChengPlusException.cast("操作失败");
            }
        }else {
            courseMarketMapper.updateById(courseMarket);
        }
        //查询课程信息
        CourseBaseInfoDto courseBaseId = getCourseBaseId(editCourseDto.getId());
        return courseBaseId;
    }

    /**
     * 删除课程
     * @param id
     */
    @Override
    @Transactional
    public void deleteCourseBase(Long id) {
        //判断课程审核状态未提交，才可删除
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (!courseBase.getAuditStatus().equals("202002")){
            XueChengPlusException.cast("只有审核状态为未提交时，才可以删除");
        }
        //删除课程基本信息
        courseBaseMapper.deleteById(id);
        //删除课程营销信息
        courseMarketMapper.deleteById(id);
        //删除课程计划信息
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId,id);
        teachplanMapper.delete(wrapper);
        //删除课程计划媒资关系信息
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getCourseId,id);
        teachplanMediaMapper.delete(teachplanMediaLambdaQueryWrapper);
        //删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> courseTeacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        courseTeacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId,id);
        courseTeacherMapper.delete(courseTeacherLambdaQueryWrapper);



    }

}
