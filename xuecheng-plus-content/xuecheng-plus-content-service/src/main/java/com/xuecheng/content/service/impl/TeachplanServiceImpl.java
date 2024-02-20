package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程计划service接口实现类
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;

    /**
     * 课程计划树形结构查询
     * @param courseId
     * @return
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * 课程计划创建或修改
     * @param teachplanDto
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        Long id = teachplanDto.getId();
        if (id==null){
            //新建课程
            //取出同父同级别的课程计划数
            LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
            teachplanLambdaQueryWrapper
                    .eq(StringUtils.isNotEmpty(String.valueOf(teachplanDto.getParentid())),Teachplan::getParentid,teachplanDto.getParentid())
                    .eq(StringUtils.isNotEmpty(String.valueOf(teachplanDto.getCourseId())),Teachplan::getCourseId,teachplanDto.getCourseId());
            Integer i = teachplanMapper.selectCount(teachplanLambdaQueryWrapper);
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            //设置排序号
            teachplan.setOrderby(i+1);
            teachplan.setCreateDate(LocalDateTime.now());
            teachplanMapper.insert(teachplan);
            CourseMarket courseMarket = courseMarketMapper.selectById(teachplanDto.getCourseId());
            //查询课程收费状态，更新关键字段
            if (courseMarket.getCharge().equals("201000")){
                teachplan.setIsPreview("1");
                teachplanMapper.updateById(teachplan);
            }


        }else {
            //修改课程
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplan.setChangeDate(LocalDateTime.now());
            //查询课程收费状态，更新关键字段
            CourseMarket courseMarket = courseMarketMapper.selectById(teachplan.getCourseId());
            if (courseMarket.getCharge().equals("201000")){
                teachplan.setIsPreview("1");
            }
            teachplanMapper.updateById(teachplan);

        }

    }

    /**
     * 删除课程计划
     * @param id
     */

    @Override
    @Transactional
    public void deleteTeachplan(Long id) {
        //根据id查询课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);
        //判断删除的是大章节还是小章节
        if (teachplan.getParentid()==0){
            //构建条件
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getParentid,teachplan.getId());
            //获取大章节的子章节数量
            Integer i = teachplanMapper.selectCount(wrapper);
            if (i>0){
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
            teachplanMapper.deleteById(id);
        //小章节删除
        }else {
            //构建删除条件
            LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
            mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId,id);
            //删除课程计划相对于的媒资数据
            teachplanMediaMapper.delete(mediaLambdaQueryWrapper);
            //删除课程计划数据
            teachplanMapper.deleteById(id);

        }


    }

    /**
     * 课程计划向下移动
     * @param id
     */
    @Override
    public void movedownTeachplan(Long id) {
        //根据id查询课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);
        //构建查询条件，查询当前课程计划的下一个课程计划
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                //父节点id
        wrapper.eq(Teachplan::getParentid,teachplan.getParentid())
                //层级
                .eq(Teachplan::getGrade,teachplan.getGrade())
                //课程id
                .eq(Teachplan::getCourseId,teachplan.getCourseId())
                //当前节点的下一个节点
                .eq(Teachplan::getOrderby,teachplan.getOrderby()+1);
        //下一个课程计划数据
        Teachplan teachplan1 = teachplanMapper.selectOne(wrapper);
        //如果是最下面则无法移动
        if (teachplan1==null){
            XueChengPlusException.cast("无法向下移动，已经最底下了");
        }
        //下一个课程计划排序字段减1
        teachplan1.setOrderby(teachplan1.getOrderby()-1);
        teachplan1.setChangeDate(LocalDateTime.now());
        //当前课程计划排序字段加一
        teachplan.setOrderby(teachplan.getOrderby()+1);
        teachplan.setChangeDate(LocalDateTime.now());
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplan1);



    }


    /**
     * 课程计划向上移动
     * @param id
     */
    @Override
    public void moveupTeachplan(Long id) {
        //根据id查询课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);
        //构建查询条件，查询当前课程计划的上一个课程计划
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                //父节点id
        wrapper.eq(Teachplan::getParentid,teachplan.getParentid())
                //层级
                .eq(Teachplan::getGrade,teachplan.getGrade())
                //课程id
                .eq(Teachplan::getCourseId,teachplan.getCourseId())
                //当前节点的上一个节点
                .eq(Teachplan::getOrderby,teachplan.getOrderby()-1);
        //上一个课程计划数据
        Teachplan teachplan1 = teachplanMapper.selectOne(wrapper);
        //如果是最上面则无法移动
        if (teachplan1==null){
            XueChengPlusException.cast("无法向上移动，已经最顶上了");
        }
        //上一个课程计划排序字段加一
        teachplan1.setOrderby(teachplan1.getOrderby()+1);
        teachplan1.setChangeDate(LocalDateTime.now());
        //当天课程计划排序字段减一
        teachplan.setOrderby(teachplan.getOrderby()-1);
        teachplan.setChangeDate(LocalDateTime.now());
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplan1);



    }

    /**
     * 课程计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return
     */
    @Override
    @Transactional
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //获取课程计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
         //根据课程计划id查询课程计划
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        //获取课程id
        Long courseId = teachplan.getCourseId();
        //构成查询条件
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据课程计划id删除
        teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId,bindTeachplanMediaDto.getTeachplanId());
        //先删除原本教学计划绑定的媒资信息
        teachplanMediaMapper.delete(teachplanMediaLambdaQueryWrapper);
        //构建封装类
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        //数据拷贝
        BeanUtils.copyProperties(bindTeachplanMediaDto,teachplanMedia);
        //设置课程id
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        //设置创建时间
        teachplanMedia.setCreateDate(LocalDateTime.now());
        //插入课程计划绑定的媒资信息
        teachplanMediaMapper.insert(teachplanMedia);



        return teachplanMedia;
    }
    /**
     * 删除课程计划和媒资信息绑定
     * @param teachPlanId
     * @param mediaId
     */
    @Override
    @Transactional
    public void deleteMedia(Long teachPlanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(TeachplanMedia::getMediaId,mediaId);

        teachplanMediaMapper.delete(queryWrapper);

    }
}
