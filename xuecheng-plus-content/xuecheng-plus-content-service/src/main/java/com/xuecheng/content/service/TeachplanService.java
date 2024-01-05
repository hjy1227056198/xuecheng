package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 课程基本信息管理业务接口
 */
public interface TeachplanService {

    /**
     * 查询课程计划树型结构
     * @param courseId
     * @return
     */
    List<TeachplanDto> findTeachplanTree(long courseId);

    /**
     * 课程计划创建或修改
     * @param teachplanDto
     */
    void saveTeachplan(SaveTeachplanDto teachplanDto);

    /**
     * 删除课程计划
     * @param id
     */
    void deleteTeachplan(Long id);

    /**
     * 课程计划向下移动
     * @param id
     */
    void movedownTeachplan(Long id);

    /**
     * 课程向上移动
     * @param id
     */
    void moveupTeachplan(Long id);
}
