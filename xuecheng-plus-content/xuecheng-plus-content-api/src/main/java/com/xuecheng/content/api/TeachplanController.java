package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程计划编辑接口
 */
@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    /**
     * 查询课程计划树形结构
     *
     * @param courseId
     * @return
     */

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    /**
     * 课程计划创建或修改
     *
     * @param teachplan
     */
    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachplanService.saveTeachplan(teachplan);
    }

    /**
     * 删除课程计划
     * @param id
     */
    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan(@PathVariable Long id){
        teachplanService.deleteTeachplan(id);
    }

    /**
     * 课程计划向下移动
     * @param id
     */
    @ApiOperation("课程计划向下移动")
    @PostMapping("/teachplan/movedown/{id}")
    public void movedownTeachplan(@PathVariable Long id){
            teachplanService.movedownTeachplan(id);
    }

    /**
     * 课程计划向上移动
     * @param id
     */
    @ApiOperation("课程计划向上移动")
    @PostMapping("/teachplan/moveup/{id}")
    public void moveupTeachplan(@PathVariable Long id){
        teachplanService.moveupTeachplan(id);
    }
}
