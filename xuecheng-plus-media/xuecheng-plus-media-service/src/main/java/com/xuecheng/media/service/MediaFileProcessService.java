package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 媒资文件处理业务方法
 */
public interface MediaFileProcessService {

    /**
     * 根据分片参数获取处理任务
     * @param shardTotal 分片总数
     * @param shardIndex  分片序号
     * @param count      任务数量
     * @return
     */

    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);


    /**
     *  开启一个任务
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    public boolean startTask(long id);

    /**
     * 跟新任务状态
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     */

    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
