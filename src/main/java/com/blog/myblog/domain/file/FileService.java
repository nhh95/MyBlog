package com.blog.myblog.domain.file;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class FileService {

    @Value("${file.temp.path}")
    private String tempFilePath;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupTempFiles() {
        System.out.println("임시 이미지 파일 정리 스케줄러 실행...");

        File tempDir = new File(tempFilePath);

        if (tempDir.exists() && tempDir.isDirectory()) {
            File[] files = tempDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    try {
                        Path filePath = file.toPath();

                        // 파일 속성 가져오기
                        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                        Instant fileCreationTime = attrs.creationTime().toInstant();

                        Instant oneDayAgo = Instant.now().minus(24, ChronoUnit.HOURS);

                        if (fileCreationTime.isBefore(oneDayAgo)) {
                            if (file.delete()) {
                                System.out.println("삭제된 파일: " + file.getName());
                            } else {
                                System.err.println("파일 삭제 실패: " + file.getName());
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("파일 처리 중 오류 발생: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("임시 이미지 파일 정리 스케줄러 종료.");
    }
}
