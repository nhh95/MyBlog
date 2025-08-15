package com.blog.myblog.controller;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;


@Controller
@RequiredArgsConstructor
public class FileController {

        @Value("${file.temp.path}")
        private String tempFilePath;

    @PostMapping(value="/uploadSummernoteImageFile", produces = "application/json")
    @ResponseBody
    public JsonObject uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {

        JsonObject jsonObject = new JsonObject();


        /*String fileRoot = "C:\\summernote_image\\temp\\";	//저장될 외부 파일 경로*/



        File file = new File(tempFilePath);
        if(!file.exists()) {
            file.mkdirs();
        }

        String originalFileName = multipartFile.getOriginalFilename();	//오리지날 파일명
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));	//파일 확장자

        String savedFileName = UUID.randomUUID() + extension;	//저장될 파일 명

        File targetFile = new File(tempFilePath + savedFileName);

        try {
            InputStream fileStream = multipartFile.getInputStream();
            FileUtils.copyInputStreamToFile(fileStream, targetFile);	//파일 저장
            jsonObject.addProperty("url", "/summernoteImage/"+savedFileName);
            jsonObject.addProperty("responseCode", "success");

        } catch (IOException e) {
            FileUtils.deleteQuietly(targetFile);	//저장된 파일 삭제
            jsonObject.addProperty("responseCode", "error");
            e.printStackTrace();
        }

        return jsonObject;
    }


    @PostMapping("/deleteSummernoteImageFile")
    @ResponseBody
    public JsonObject deleteSummernoteImageFile(@RequestParam("imageUrl") String imageUrl) {


        JsonObject jsonObject = new JsonObject();

        try {
            // URL에서 파일명 추출 (예: /summernoteImage/abc.png -> abc.png)
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // 임시 폴더에 있는 파일 경로
            File imageFile = new File(tempFilePath + fileName);

            if (imageFile.exists()) {
                if (imageFile.delete()) {
                    jsonObject.addProperty("responseCode", "success");
                } else {
                    jsonObject.addProperty("responseCode", "error");
                    System.err.println("File deletion failed: " + fileName);
                }
            } else {
                jsonObject.addProperty("responseCode", "success"); // 파일이 이미 없으면 성공으로 간주
            }
        } catch (Exception e) {
            jsonObject.addProperty("responseCode", "error");
            e.printStackTrace();
        }

        return jsonObject;
    }
}
