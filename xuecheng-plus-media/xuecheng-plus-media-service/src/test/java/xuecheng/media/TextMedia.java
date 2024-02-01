package xuecheng.media;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class TextMedia {
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    // 上传文件
    @Test
    public  void upload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        UploadObjectArgs build = UploadObjectArgs.builder()
                .bucket("textbucket")// 存储桶名
                .object("1.png")// 上传到minio后的文件名
                .filename("F:\\学成在线项目—资料\\day05 媒资管理 Nacos Gateway MinIO\\资料\\屏幕截图 2022-04-28 171207.png")// 本地文件路径
                .build();

        minioClient.uploadObject(build);
    }


    @Test
    public void test_upload() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        RemoveObjectArgs build = RemoveObjectArgs.builder()
                .bucket("textbucket")
                .object("test.doc")
                .build();

        minioClient.removeObject(build);
    }
    //上传分块到minio
    @Test
    public void uploadChunk(){
        try {

            for (int i = 0; i < 5; i++) {
                minioClient.uploadObject(
                        UploadObjectArgs.builder()
                                .bucket("textbucket")
                                .object("chuuk/"+i)
                                .filename("F:\\学成在线项目—资料\\1.mp4")
                                .build());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将minio中的分片合并

    @Test
    public void testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ComposeSource>  list=new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(ComposeSource.builder().bucket("textbucket").object("chuuk/"+i).build());
        }


        ComposeObjectArgs build = ComposeObjectArgs.builder()
                .bucket("textbucket")
                .object("ME.mp4")
                .sources(list)
                .build();


        minioClient.composeObject(build);
    }
}
