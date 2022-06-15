package cool.yunlong.mall.product.controller;

import cool.yunlong.mall.common.result.Result;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author yunlong
 * @since 2022/6/13 15:37
 */
@Api(tags = "文件上传接口")
@RestController
@RequestMapping("/admin/product")
@RefreshScope
public class FileUploadController {

    @Value("${minio.endpointUrl}")
    public String endpointUrl;

    @Value("${minio.accessKey}")
    public String accessKey;

    @Value("${minio.secretKey}")
    public String secretKey;

    @Value("${minio.bucketName}")
    public String bucketName;

    @PostMapping("/fileUpload")
    public Result<String> fileUpload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        // 创建 MinioClient 对象
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(endpointUrl)
                        .credentials(accessKey, secretKey)
                        .build();
        // 判断存储桶是否存在
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (isExist) {
            System.out.println("Bucket already exists.");
        } else {
            // 创建存储桶
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
        //  设置文件名称    必须是唯一标识,不能重复
        String fileName = System.currentTimeMillis() + UUID.randomUUID().toString();

        // 上传文件 参数1:存储桶名称  参数2:文件名称  参数3:文件流
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
        //  文件上传之后的路径： http://39.99.159.121:9000/mall/xxxxxx
        String url = endpointUrl + "/" + bucketName + "/" + fileName;

        System.out.println("url:\t" + url);
        //  将文件上传之后的路径返回给页面！
        return Result.ok(url);
    }
}
