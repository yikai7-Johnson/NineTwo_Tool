package com.ninetwo.boot.controller;

import com.alibaba.fastjson.JSON;
import com.ninetwo.boot.util.FileUtils;
import com.ninetwo.boot.util.pdftowordSprire.PdfToWord;
import com.ninetwo.boot.util.pdftowordSprire.WordToPdf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PdfController {

    @Autowired
    private PdfToWord pdfToWord;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private WordToPdf wordToPdf;

    @PostMapping("/pdf/toword")
    public ResponseEntity<String> pdfToWord(@RequestParam("file") MultipartFile fileUpload) throws IOException {
        try {

            String currentTime = System.nanoTime() + "";
            String pdfName = fileUtils.getFileStorage() + currentTime + ".pdf";
            File file = new File(pdfName);
            fileUpload.transferTo(file);
            file.deleteOnExit();

            boolean r = pdfToWord.pdftoword(pdfName);

            // 返回成功响应
            Map<String, Object> res = new HashMap<>();
            res.put("flag", r);
            String oldname = fileUpload.getOriginalFilename();
            res.put("oldname", oldname.substring(0, oldname.length() - 4) + ".docx");
            res.put("filename", currentTime + ".docx");

            return ResponseEntity.ok(JSON.toJSONString(res));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("null");
    }

    @PostMapping("/word/topdf")
    public ResponseEntity<String> wordToPdf(@RequestParam("file") MultipartFile fileUpload) throws IOException {
        try {

            String currentTime = System.nanoTime() + "";
            String pdfName = fileUtils.getFileStorage() + currentTime + ".docx";
            File file = new File(pdfName);
            fileUpload.transferTo(file);
            file.deleteOnExit();

            boolean r = wordToPdf.pdftoword(pdfName);

            // 返回成功响应
            Map<String, Object> res = new HashMap<>();
            res.put("flag", r);
            String oldname = fileUpload.getOriginalFilename();
            res.put("oldname", oldname.substring(0, oldname.lastIndexOf(".")) + ".pdf");
            res.put("filename", currentTime + ".pdf");

            return ResponseEntity.ok(JSON.toJSONString(res));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("null");
    }
}
