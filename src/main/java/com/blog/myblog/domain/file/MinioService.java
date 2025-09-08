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

        System.out.println("=== MinIO 업로드 시작 ===");
        System.out.println("파일명: " + file.getName());
        System.out.println("파일 크기: " + file.length());
        System.out.println("파일 존재 여부: " + file.exists());
        System.out.println("파일 읽기 가능: " + file.canRead());
        System.out.println("버킷명: " + bucketName);
        System.out.println("MinIO URL: " + minioUrl);


        try (InputStream inputStream = new FileInputStream(file)) {

            // 파일명 중복 방지를 위해 UUID와 원본 파일명 조합
            String minioFileName = UUID.randomUUID().toString() + "_" + file.getName();
            String minioObjectPath = "summernoteImage/" + minioFileName;

            System.out.println("MinIO 파일명: " + minioFileName);
            System.out.println("MinIO 객체 경로: " + minioObjectPath);

            // MinIO 버킷에 파일 업로드
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(minioObjectPath)
                            .stream(inputStream,file.length(),-1)
                            .contentType(Files.probeContentType(file.toPath()))
                            .build()
            );

            String resultUrl = minioUrl + "/" + bucketName + "/summernoteImage/" + minioFileName;
            System.out.println("업로드 성공 결과 URL: " + resultUrl);
            return resultUrl;

        } catch (Exception e) {
            System.err.println("MinIO 업로드 실패");
            System.err.println("에러 타입: " + e.getClass().getSimpleName());
            System.err.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();

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
