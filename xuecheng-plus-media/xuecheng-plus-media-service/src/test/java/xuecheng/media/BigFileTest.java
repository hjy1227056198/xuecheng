package xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static javafx.scene.input.KeyCode.F;

/**
 * 测试大文件上传方法
 */
public class BigFileTest {

    //分块测试
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("F:\\学成在线项目—资料\\1.mp4");
        //分块存储路径
        String chunkPath = "F:\\学成在线项目—资料\\chunk\\";
        //分块大小
        long chunkSize = 5*1024*1024;
        //分块数量
        int chunkNum =  (int) Math.ceil(sourceFile.length()*1.0/chunkSize);
        //使用流从源文件读数据
        RandomAccessFile re_r = new RandomAccessFile(sourceFile, "r");
        //缓冲区
        byte[] b = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File file = new File(chunkPath + i);

            RandomAccessFile rw = new RandomAccessFile(file, "rw");

            int len= -1;

            while ((len = re_r.read(b))!=-1){
                rw.write(b,0,len);
                if (file.length()>=chunkSize){
                    break;
                }
            }
            rw.close();
        }
        re_r.close();


    }


    //合并测试
    @Test
    public void testMerge() throws IOException {

        //分块文件目录
        File chunkFolder = new File("F:\\学成在线项目—资料\\chunk\\");

        //源文件
        File sourceFile = new File("F:\\学成在线项目—资料\\1.mp4");

        //合并文件
        File mergeFile = new File("F:\\学成在线项目—资料\\1_merge.mp4");
        //取出分块文件
        File[] files = chunkFolder.listFiles();

        List<File> list = Arrays.asList(files);

        //排序
        list.sort((o1, o2) -> {
            if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                return 1;
            }
            return -1;
        });
        //向合并文件写的流
        RandomAccessFile rw = new RandomAccessFile(mergeFile, "rw");
        //缓冲区
        byte[] b = new byte[1024];
        for (File file : list) {
            RandomAccessFile r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = r.read(b)) != -1) {
                rw.write(b, 0, len);
            }
            r.close();
        }
        rw.close();
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        String fileMd5 = DigestUtils.md5Hex(fileInputStream);
        FileInputStream fileInputStream1 = new FileInputStream(mergeFile);
        String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream1);
        if (fileMd5.equals(mergeFileMd5)) {
            System.out.println("文件合并成功");
        }
    }
}
