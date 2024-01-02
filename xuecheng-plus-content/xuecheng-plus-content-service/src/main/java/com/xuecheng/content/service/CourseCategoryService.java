package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {
    /**
     * 课程分类查询
     * @param id
     * @return
     */

    List<CourseCategoryTreeDto> queryTreeModes(String id);
}
