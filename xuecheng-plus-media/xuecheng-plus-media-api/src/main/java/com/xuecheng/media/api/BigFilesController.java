package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 大文件上传接口
 * @date 2022/9/6 11:29
 */
@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
public class BigFilesController {
    @Autowired
    private MediaFileService mediaFileService;


    /**
     * 文件上传前检查文件
     * @param fileMd5
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(
            @RequestParam("fileMd5") String fileMd5
    ) throws Exception {

        RestResponse<Boolean> checkfile = mediaFileService.checkFile(fileMd5);
        return checkfile;
    }

    /**
     * 分块文件上传前的检测
     * @param fileMd5
     * @param chunk
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {

        RestResponse<Boolean> checkChunk = mediaFileService.checkChunk(fileMd5, chunk);
       return checkChunk;
    }

    /**
     * 上传分块文件
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {
        File minin = File.createTempFile("minin", ".tmp");
        file.transferTo(minin);
        String absolutePath = minin.getAbsolutePath();
        RestResponse uploadchunk = mediaFileService.uploadChunk(fileMd5, chunk, absolutePath);
        return uploadchunk;
    }

    /**
     * 合并文件
     * @param fileMd5
     * @param fileName
     * @param chunkTotal
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {

        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileType("001002");
        uploadFileParamsDto.setTags("课程视频");
        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setFilename(fileName);
        RestResponse mergechunks = mediaFileService.mergechunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);

        return mergechunks;

    }


}