package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @version 1.0
 */
 @Service
 @Slf4j
public class MediaFileServiceImpl implements MediaFileService {

 @Autowired
 MediaFilesMapper mediaFilesMapper;
 @Autowired
 private MinioClient minioClient;
 //普通文件桶
 @Value("${minio.bucket.files}")
 private String bucket_Files;
 //视频文件桶
 @Value("${minio.bucket.videofiles}")
 private String bucket_Video;

 @Autowired
 private MediaProcessMapper mediaProcessMapper;


 @Autowired
 private MediaFileServiceImpl mediaFileService;

 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  queryWrapper.eq(StringUtils.isNotEmpty(queryMediaParamsDto.getFileType()),MediaFiles::getFileType,queryMediaParamsDto.getFileType())
          .like(StringUtils.isNotEmpty(queryMediaParamsDto.getFilename()),MediaFiles::getFilename,queryMediaParamsDto.getFilename());
  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }

 //获取文件默认存储目录路径 年/月/日
 private String getDefaultFolderPath() {
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  String folder = sdf.format(new Date()).replace("-", "/") + "/";
  return folder;
 }

 //获取文件的md5
 private String getFileMd5(File file) {
  try (FileInputStream fileInputStream = new FileInputStream(file)) {
   String fileMd5 = DigestUtils.md5Hex(fileInputStream);
   return fileMd5;
  } catch (Exception e) {
   e.printStackTrace();
   return null;
  }
 }

 //获取文件类型
 private String getMimeType(String extension) {
  if (extension == null)
   extension = "";
  //根据扩展名取出mimeType
  ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
  //通用mimeType，字节流
  String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
  if (extensionMatch != null) {
   mimeType = extensionMatch.getMimeType();
  }
  return mimeType;
 }

 /**
  * @param localFilePath 文件地址
  * @param bucket        桶
  * @param objectName    对象名称
  * @return void
  * @description 将文件写入minIO
  * @author Mr.M
  */
 public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
  try {
   UploadObjectArgs testbucket = UploadObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .filename(localFilePath)
           .contentType(mimeType)
           .build();
   minioClient.uploadObject(testbucket);
   log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
   System.out.println("上传成功");
   return true;
  } catch (Exception e) {
   e.printStackTrace();
   log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket, objectName, e.getMessage(), e);
   XueChengPlusException.cast("上传文件到文件系统失败");
  }
  return false;
 }



 /**
  * @param companyId           机构id
  * @param fileMd5             文件md5值
  * @param uploadFileParamsDto 上传文件的信息
  * @param bucket              桶
  * @param objectName          对象名称
  * @return com.xuecheng.media.model.po.MediaFiles
  * @description 将文件信息添加到文件表
  * @author Mr.M
  */
 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
  //从数据库查询文件
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles == null) {
   mediaFiles = new MediaFiles();
   //拷贝基本信息
   BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
   mediaFiles.setId(fileMd5);
   mediaFiles.setFileId(fileMd5);
   mediaFiles.setCompanyId(companyId);
   mediaFiles.setUrl("/" + bucket + "/" + objectName);
   mediaFiles.setBucket(bucket);
   mediaFiles.setFilePath(objectName);
   mediaFiles.setCreateDate(LocalDateTime.now());
   mediaFiles.setAuditStatus("002003");
   mediaFiles.setStatus("1");
   //保存文件信息到文件表
   int insert = mediaFilesMapper.insert(mediaFiles);

   if (insert < 0) {
    log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
    XueChengPlusException.cast("保存文件信息失败");
   }
   //添加到待处理任务表
   addWaitingTask(mediaFiles);
   log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());

  }
  return mediaFiles;

 }
 /**
  * 添加待处理任务
  * @param mediaFiles 媒资文件信息
  */
 public void addWaitingTask(MediaFiles mediaFiles){

         //文件名称
         String filename = mediaFiles.getFilename();
         //文件扩展名
         String fileExt = filename.substring(filename.lastIndexOf("."));
            //文件类型
         String mimeType = getMimeType(fileExt);
         //旁段上传的视频是否为avi格式
         if (mimeType.equals("video/x-msvideo")){

          MediaProcess mediaProcess = new MediaProcess();
          //数据拷贝
          BeanUtils.copyProperties(mediaFiles,mediaProcess);

          //设置状态值为待处理
          mediaProcess.setStatus("1");
          //设置默认失败次数为0
//          mediaProcess.setFailCount(0);
          mediaProcess.setCreateDate(LocalDateTime.now());
          mediaProcess.setUrl(null);
          //插入数据库
          mediaProcessMapper.insert(mediaProcess);
         }

 }

 /**
  * 上传文件
  *
  * @param companyId           机构id
  * @param uploadFileParamsDto 上传文件信息
  * @param localFilePath       文件磁盘路径
  * @return 文件信息
  */
 @Override
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {

  //文件名
  String filename = uploadFileParamsDto.getFilename();
  //文件扩展名
  String extension = filename.substring(filename.lastIndexOf("."));
  //得到文件类型
  String mimeType = getMimeType(extension);
  //子目录
  String defaultFolderPath = getDefaultFolderPath();
  //文件md5
  String fileMd5 = getFileMd5(new File(localFilePath));


  if (StringUtils.isEmpty(objectName)){
   objectName= defaultFolderPath + fileMd5 + extension;
  }
  //最终存储在minIO的文件名

  //上传文件到minIO
  addMediaFilesToMinIO(localFilePath, mimeType, bucket_Files, objectName);
  //将文件信息添加到文件表
  MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_Files, objectName);
  //构造封装类
  UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
  //拷贝基本信息
  BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
  return uploadFileResultDto;
 }

 /**
  * @param fileMd5 文件的md5
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  * @description 检查文件是否存在
  * @author Mr.M
  */
 @Override
 public RestResponse<Boolean> checkFile(String fileMd5) {
  //从数据库查询文件
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles != null) {
   //桶
   String bucket = mediaFiles.getBucket();
   //存储目录
   String filePath = mediaFiles.getFilePath();
   //从minIO查询文件
   GetObjectArgs build = GetObjectArgs.builder()
           .bucket(bucket)
           .object(filePath)
           .build();
   try {
    InputStream object = minioClient.getObject(build);
    //存在返回true
    if (object != null) {
     return RestResponse.success(true);
    }
   } catch (Exception e) {
    e.printStackTrace();
   }


  }

  //不存在返回false
  return RestResponse.success(false);
 }

 /**
  * @param fileMd5    文件的md5
  * @param chunkIndex 分块序号
  * @description 检查分块是否存在
  * @author Mr.M
  */
 @Override
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

  //得到分块文件所在目录
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

  InputStream object = null;
  try {
   GetObjectArgs build = GetObjectArgs.builder()
           .bucket(bucket_Video)
           .object(chunkFileFolderPath + chunkIndex)
           .build();

   object = minioClient.getObject(build);

   if (object != null) {
    return RestResponse.success(true);
   }
  } catch (Exception e) {
   e.printStackTrace();
  }


  return RestResponse.success(false);
 }


 //得到分块文件的目录
 private String getChunkFileFolderPath(String fileMd5) {
  return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
 }


 /**
  * @param fileMd5 文件md5
  * @param chunk   分块序号
  * @return com.xuecheng.base.model.RestResponse
  * @description 上传分块
  * @author Mr.M
  */
 @Override
 public RestResponse uploadChunk(String fileMd5, int chunk, String localFilePath) {

  //得到分块文件的目录路径
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //得到分块文件的路径
  String chunkFilePath = chunkFileFolderPath + chunk;
  String mimeType = getMimeType(null);
  try {
   //将文件存储至minIO
   boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_Video, chunkFilePath);
    if (b) {
     return RestResponse.success(true);
    }
  } catch (Exception ex) {
   ex.printStackTrace();
   log.debug("上传分块文件:{},失败:{}", chunkFilePath, ex.getMessage());
  }
  return RestResponse.validfail(false, "上传分块失败");
  }

 /**
  * 从minio下载文件
  * @param bucket 桶
  * @param objectName 对象名称
  * @return 下载后的文件
  */
 public File downloadFileFromMinIO(String bucket,String objectName){
  //临时文件
  File minioFile = null;
  FileOutputStream outputStream = null;
  try{
   InputStream stream = minioClient.getObject(GetObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .build());
   //创建临时文件
   minioFile=File.createTempFile("minio", ".merge");
   outputStream = new FileOutputStream(minioFile);
   IOUtils.copy(stream,outputStream);
   return minioFile;
  } catch (Exception e) {
   e.printStackTrace();
  }finally {
   if(outputStream!=null){
    try {
     outputStream.close();
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }
  return null;
 }

 /**
  * //得到合并后的文件地址
  * @param fileMd5 文件md5
  * @param fileExt 文件扩展名
  * @return
  */
 private String getFilePathbyMd5(String fileMd5,String fileExt){

  return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5+fileExt;
 }


 /**
  * 清除分块文件
  * @param chunkFileFolderPath 分块文件路径
  * @param chunkTotal 分块文件总数
  */
 private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

  try {
   List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
           .limit(chunkTotal)
           .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
           .collect(Collectors.toList());

   RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_Video).objects(deleteObjects).build();
   Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
   results.forEach(r->{
    DeleteError deleteError = null;
    try {
     deleteError = r.get();
    } catch (Exception e) {
     e.printStackTrace();
     log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
    }
   });
  } catch (Exception e) {
   e.printStackTrace();
   log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
  }
 }

 /**
  * 合并分块
  * @param companyId
  * @param fileMd5
  * @param chunkTotal
  * @param uploadFileParamsDto
  * @return
  */
 @Override
 public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
  //定义数据列表
  List<ComposeSource>  list=new ArrayList<>();
  //根据md5值得到minio中的文件分区路径
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
    //得到文件名
  String filename = uploadFileParamsDto.getFilename();
  //得到文件扩展名
  String substring = filename.substring(filename.lastIndexOf(".") );
    //得到合并后的文件地址
  String filePathbyMd5 = getFilePathbyMd5(fileMd5, substring);

  for (int i = 0; i < chunkTotal; i++) {
   list.add(ComposeSource.builder().bucket(bucket_Video).object(chunkFileFolderPath+i).build());
  }


  ComposeObjectArgs build = ComposeObjectArgs.builder()
          .bucket(bucket_Video)
          .object(filePathbyMd5)
          .sources(list)
          .build();


  try {
   //合并分块文件
   minioClient.composeObject(build);
  } catch (Exception e) {
   e.printStackTrace();
   log.error("合并分块文件失败,bucket:{},objectName:{},错误信息：{}",bucket_Video,filePathbyMd5,e.getMessage());
   return RestResponse.validfail(false,"合并分块文件失败");
  }
 //从minio下载文件
  File file = downloadFileFromMinIO(bucket_Video, filePathbyMd5);
  try(FileInputStream fileInputStream=new FileInputStream(file)) {
   //校验合并后的文件是否和前端传入的md5值一致
    String md5Hex = DigestUtils.md5Hex(fileInputStream);
    if(!md5Hex.equals(fileMd5)){
     log.error("合并后的文件md5值与前端传入的md5值不一致,合并后的文件md5值:{},前端传入的md5值:{}",md5Hex,fileMd5);
     return RestResponse.validfail(false,"文件校验失败");

    }
    uploadFileParamsDto.setFileSize(file.length());
  }catch (Exception e){
   return RestResponse.validfail(false,"文件校验失败");
  }

  //将文件信息添加到文件表
    MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_Video, filePathbyMd5);
  if (mediaFiles==null){
   log.error("保存文件信息失败");
   return RestResponse.validfail(false,"保存文件信息失败");
  }
  //清理文件
    clearChunkFiles(chunkFileFolderPath,chunkTotal);



  return RestResponse.success(true);
 }

 /**
  * 根据媒资id获取媒资信息
  * @param mediaId
  * @return
  */
 @Override
 public MediaFiles getFileById(String mediaId) {
  MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
  return mediaFiles;
 }



}
