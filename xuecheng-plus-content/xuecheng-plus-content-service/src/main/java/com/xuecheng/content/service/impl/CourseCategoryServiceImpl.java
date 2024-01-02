package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    /**
     * 课程分类查询
     * @param id
     * @return
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeModes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTree = courseCategoryMapper.selectTreeModes(id);
        //转成map类型
        Map<String, CourseCategoryTreeDto> map = courseCategoryTree.stream()
                //过滤根节点
                .filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //定义返回最终数据
        List<CourseCategoryTreeDto> list=new ArrayList<>();
        courseCategoryTree.stream()
                //过滤根节点
                .filter(item -> !id.equals(item.getId()))
                .forEach(item ->{
                    //如果是主节点，则添加到list集合
                    if (id.equals(item.getParentid())){
                        list.add(item);
                    }
                    //获取当前操作的节点的父节点
                    CourseCategoryTreeDto courseCategoryTreeDto = map.get(item.getParentid());
                    //判断当前节点的父节点是否为空
                    if (courseCategoryTreeDto!=null){
                        //如果不为空则，给父节点中的子节点new一个list，防止空指针异常
                        if (courseCategoryTreeDto.getChildrenTreeNodes()==null){
                            courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        //把当前操作的节点，添加到他的父节点中的集合
                        courseCategoryTreeDto.getChildrenTreeNodes().add(item);
                    }
                });
        return list;
    }
}
