package com.ninetwo.boot.util.pdftowordSprire;

import com.aspose.words.*;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class WordToPdf {

    public boolean pdftoword(String  srcPath) {
        // 4、最终生成的doc所在的目录，默认是和引入的一个地方，开源时对外提供下载的接口。
        String desPath = srcPath.substring(0, srcPath.lastIndexOf(".")) + ".pdf";
        boolean result = false;
        try {
            // 0、判断输入的是否是pdf文件
            //第一步：判断输入的是否合法
            boolean flag = isPDFFile(srcPath);

            if (flag) {




                // 源文件
                com.aspose.words.Document doc = new com.aspose.words.Document(srcPath);


                //  将windows中字体库C:\Windows\Fonts复制到linux中或docker镜像中/usr/share/fonts/win
                FontSourceBase[] originalFontSources = FontSettings.getDefaultInstance().getFontsSources();//默认字体源
                // 从包含字体的文件夹创建字体源。
                FolderFontSource folderFontSource = new FolderFontSource("/usr/share/fonts/win", true);
                // 应用一个新的字体源数组，其中包含原始字体源以及我们的自定义字体。
                FontSourceBase[] updatedFontSources = {originalFontSources[0], folderFontSource};
                FontSettings.getDefaultInstance().setFontsSources(updatedFontSources);


                // 获取控制修订外观的RevisionOptions对象
                RevisionOptions revisionOptions = doc.getLayoutOptions().getRevisionOptions();
                // 在引出序号中显示删除修订
                revisionOptions.setShowInBalloons(ShowInBalloons.FORMAT_AND_DELETE);
                // 输出
                doc.save(desPath);

            } else {
                System.out.println("输入的不是pdf文件");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //4、把刚刚缓存的split和doc删除
            if (result == true) {
//                new FileDeleteTest().clearFiles(splitPath);
//                new FileDeleteTest().clearFiles(docPath);
            }
        }
        return true;
    }

    // 判断是否是pdf文件
    private  boolean isPDFFile(String srcPath2) {
        File file = new File(srcPath2);
        String filename = file.getName();
        if (filename.endsWith(".docx")) {
            return true;
        }
        return false;
    }

}
