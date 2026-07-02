package com.ninetwo.boot.controller;

import com.aspose.words.*;
import com.ninetwo.boot.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/TxtTo")
public class TxtToPdfController {

    @Autowired
    private FileUtils fileUtils;

    @PostMapping("/toPdf")
    public ResponseEntity<String> toPdf(@RequestBody Map<String, String> body) throws IOException {
        try {
            String text = body.get("text");
            System.out.println("Received text length: " + (text != null ? text.length() : 0));

            String substring = "";
            if (text != null && text.indexOf("</think>") != -1) {
                substring = text.substring(text.lastIndexOf("</think>") + 8);
            } else {
                substring = text != null ? text : "";
            }

            // 创建文档
            com.aspose.words.Document doc = new com.aspose.words.Document();
            DocumentBuilder builder = new DocumentBuilder(doc);
            builder.write(substring);

            // 保存到文件存储目录
            String filename = new Date().getTime() + ".pdf";
            String desPath = fileUtils.getFileStorage() + filename;

            // 确保目录存在
            File dir = new File(fileUtils.getFileStorage());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            doc.save(desPath, SaveFormat.PDF);

            System.out.println("PDF saved to: " + desPath);

            // 返回成功响应
            Map<String, Object> res = new HashMap<>();
            res.put("filename", filename);
            res.put("oldname", "document.pdf");
            res.put("path", desPath);
            return ResponseEntity.ok(com.alibaba.fastjson.JSON.toJSONString(res));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to convert: " + e.getMessage());
        }
    }
}
