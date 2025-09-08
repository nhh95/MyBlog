package com.blog.myblog.domain.file;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {


    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;


    //minio에 이미지 전송
    public String minioUploadFile(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {

            // 파일명 중복 방지를 위해 UUID와 원본 파일명 조합
            String minioFileName = UUID.randomUUID().toString() + "_" + file.getName();
            String minioObjectPath = "summernoteImage/" + minioFileName;
            // MinIO 버킷에 파일 업로드
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(minioObjectPath)
                            .stream(inputStream,file.length(),-1)
                            .contentType(Files.probeContentType(file.toPath()))
                            .build()
            );

            return minioUrl + "/" + bucketName + "/summernoteImage/" + minioFileName;

        } catch (Exception e) {
            throw new RuntimeException("MinIO UploadFile failed", e);
        }
    }


    // MinIO에서 파일을 삭제합니다.
    public void minioDeleteFile(String minioFileName) {
        try {
            String minioObjectPath = "summernoteImage/" + minioFileName;
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(minioObjectPath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("miniO DeleteFile failed", e);
        }
    }


}
