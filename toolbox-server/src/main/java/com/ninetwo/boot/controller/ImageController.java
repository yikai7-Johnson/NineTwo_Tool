package com.ninetwo.boot.controller;

import com.alibaba.fastjson.JSON;
import com.benjaminwan.ocrlibrary.OcrResult;
import com.ninetwo.boot.service.ImageService;
import com.ninetwo.boot.util.FileUtils;
import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.github.mymonstercat.ocr.config.ParamConfig;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/compress", method = RequestMethod.POST)
    public ResponseEntity<String> compressImage(@RequestParam(name = "file", required = false) MultipartFile file,
                                                               @RequestParam String width, @RequestParam String height,
                                                               @RequestParam Double quality, @RequestParam String suffix ) {
        try {
            System.out.println("图片名：" + file.getName() + ",宽：" + width + ",高：" + height + ",质量：" + quality + ",格式：" + suffix);
            String filename = new Date().getTime() + "." + suffix;
            File fileStorge = new File(fileUtils.getFileStorage());
            if (!fileStorge.exists()) {
                fileStorge.mkdirs();
            }
            // 创建一个ByteArrayOutputStream来存储生成的缩略图
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 使用Thumbnailator生成缩略图
            Thumbnails.of(file.getInputStream())
                    .size(Integer.valueOf(width), Integer.valueOf(height)) // 设置缩略图的宽度和高度
                    .outputQuality(quality) // 设置输出质量
                    .outputFormat(suffix) // 设置输出格式
                    .toFile(fileUtils.getFileStorage() + filename);
//                    .toOutputStream(outputStream);

            // 获取缩略图的字节数组
            byte[] thumbnailBytes = outputStream.toByteArray();

            // 在这里，你可以将缩略图保存到文件系统，或者将其发送到其他服务
            // 例如，保存到文件系统：
            //String base64Encoded = Base64.getEncoder().encodeToString(thumbnailBytes);

            //Files.write(Paths.get(imgStorage + filename), thumbnailBytes);

            File newFile = new File(fileUtils.getFileStorage() + filename);
            System.out.println("大小：" + newFile.length() + ",宽：" + width + ",高：" + height + ",质量：" + quality + ",格式：" + suffix);

            // 返回成功响应
            Map<String, Object> res = new HashMap<>();
            res.put("name", filename);
            res.put("size",  newFile.length());
            return ResponseEntity.ok(JSON.toJSONString(res));
        } catch (IOException e) {
            e.printStackTrace();
            // 返回错误响应
            return ResponseEntity.badRequest().body("Failed to create thumbnail");
        }
    }

    /**
     * 直接通过 Resource 对象返回文件资源，Spring 会自动处理资源的读取和流式传输。
     * 使用 HttpHeaders.CONTENT_DISPOSITION 设置响应头，强制浏览器将文件作为附件下载（attachment）。
     * 如果资源不存在或不可读，抛出 FileNotFoundException 并返回错误状态码（如 404）。
     * @param imageName
     * @param request
     * @return
     */
    @GetMapping("/image/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName, HttpServletRequest request) {
        try {
            Path file = Paths.get(fileUtils.getFileStorage(), imageName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new FileNotFoundException("Could not read the image: " + imageName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read the image: " + imageName, e);
        }
    }

    /**
     * 手动读取文件并写入 HTTP 响应的输出流（OutputStream），实现流式传输。
     * 需要显式处理异常（如 IOException）并手动关闭资源（FileInputStream 和 OutputStream）。
     * 没有设置 Content-Disposition 头，可能导致浏览器默认以“预览”方式显示图片（而非下载）。
     * @param imageName
     * @param response
     */
    @RequestMapping(value = "/previewImage/{imageName}", method = RequestMethod.GET)
    public void previewImage(@PathVariable String imageName, HttpServletResponse response) {
        try {
            File imageFile = new File(fileUtils.getFileStorage() + imageName);
            FileInputStream fis = new FileInputStream(imageFile);
            OutputStream out = response.getOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            fis.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/ocr")
    public ResponseEntity<Map<String, Object>> ocr(@RequestParam("file") MultipartFile fileUpload) throws IOException {
        try {
            ParamConfig paramConfig = ParamConfig.getDefaultConfig();
            paramConfig.setDoAngle(true);
            paramConfig.setMostAngle(true);
            InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V3);

            String imgName1 = fileUtils.getFileStorage() + System.nanoTime() + ".png";
            String imgName2 = fileUtils.getFileStorage() + System.nanoTime() + ".png";
            File file = new File(imgName1);
            fileUpload.transferTo(file);
            file.deleteOnExit();

            OcrResult ocrResult = engine.runOcr(file.getPath(),paramConfig);
            String s = imageService.OcrResultDrawer(ocrResult, imgName1, imgName2, fileUtils.getFileStorage());

            // 返回成功响应
            Map<String, Object> res = new HashMap<>();
            res.put("strRes", ocrResult.getStrRes());
            res.put("base64",  s);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(Collections.emptyMap());
    }
}
