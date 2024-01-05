package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

//修改课程模型数据
@Data
public class EditCourseDto extends AddCourseDto{


    @ApiModelProperty(value = "id", required = true)
    private Long id;


}
