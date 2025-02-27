package org.example.backend_1.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author xuwenbin
 */
@Component
public class AliOSSUtils {
    private final String endpoint = System.getenv("AliOSSEndpoint");
    private final String accessKeyId = System.getenv("AliOSSAccessKeyId");
    private final String accessKeySecret = System.getenv("AliOSSAccessKeySecret");
        private final String bucketName = System.getenv("AliOSSBucketName");

    /**
     * 实现上传图片到OSS
     */
    public String upload(MultipartFile multipartFile) throws IOException {
        // 获取上传的文件的输入流
        InputStream inputStream = multipartFile.getInputStream();

        // 避免文件覆盖
        String originalFilename = multipartFile.getOriginalFilename();
        String fileName = null;
        if (originalFilename != null) {
            fileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        //文件访问路径
        String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;

        // 关闭ossClient
        ossClient.shutdown();
        // 把上传到oss的路径返回
        return url;
    }
}