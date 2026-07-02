package com.ninetwo.boot.util;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Data
@Component
public class FileUtils {

    private String createFolder(String path){
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return path;
    }

    // 涉及到的路径
    // 1、pdf所在的路径，真实测试种是从外部引入的
    // 2、如果是大文件，需要进行切分，保存的子pdf路径
    private String splitPath;

    // 3、如果是大文件，需要对子pdf文件一个一个进行转化
    private String docPath;

    private String FileStorage;
    @Value("${file-storage.path}")
    private void setFileStorage(String path){
        FileStorage = path;
        splitPath = createFolder(path + "split"  + File.separator);
        docPath = createFolder(path + "doc"  + File.separator);
    }







}
