package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


/**
 * 测试Freemaker的测试方法
 */
@SpringBootTest
public class FreemarkerTest {

    @Autowired
    CoursePublishService coursePublishService;

    //页面静态化测试
    @Test
    public void testGenerateHtmlByTemplate() throws Exception {

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
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(117L);

        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("model",coursePreviewInfo);

        String s = FreeMarkerTemplateUtils.processTemplateIntoString(template, stringObjectHashMap);

        //输入流
        InputStream inputStream = IOUtils.toInputStream(s, "utf-8");
        //输出文件
        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:\\学成在线项目—资料\\117.html"));
        IOUtils.copy(inputStream,fileOutputStream);

    }
}
