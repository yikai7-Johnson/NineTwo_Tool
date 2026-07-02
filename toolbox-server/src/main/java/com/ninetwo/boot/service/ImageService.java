package com.ninetwo.boot.service;

import com.benjaminwan.ocrlibrary.OcrResult;
import com.benjaminwan.ocrlibrary.TextBlock;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Service
public class ImageService {


    /**
     * 给图片中 文本 画框
     * @param ocrResult
     * @param originalImagePath
     * @param newImagePath
     */
    public String OcrResultDrawer(OcrResult ocrResult, String originalImagePath, String newImagePath, String imgStorage) {
        try {
            // 假设原始图片的路径
//        String originalImagePath = "D:\\temp\\ocr\\test.png";

            // 加载原始图片
            BufferedImage originalImage = null;
            try {
                originalImage = ImageIO.read(new File(originalImagePath));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            // 创建一个与原始图片大小相同的图片用于绘制
            BufferedImage resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resultImage.createGraphics();

            // 将原始图片绘制到新图片上
            g2d.drawImage(originalImage, 0, 0, null);

            // 设置矩形框的颜色和样式
            g2d.setColor(Color.RED);
            g2d.setStroke(new java.awt.BasicStroke(2));

            // 遍历所有的 TextBlock 并绘制矩形框
            for (TextBlock textBlock : ocrResult.getTextBlocks()) {
                Rectangle rect = new Rectangle(
                        textBlock.getBoxPoint().get(0).getX(),
                        textBlock.getBoxPoint().get(0).getY(),
                        textBlock.getBoxPoint().get(2).getX() - textBlock.getBoxPoint().get(0).getX(),
                        textBlock.getBoxPoint().get(2).getY() - textBlock.getBoxPoint().get(0).getY()
                );
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
            }

            // 释放 Graphics2D 对象
            g2d.dispose();

            // 保存结果图片
            try {
//            ImageIO.write(resultImage, "png", new File("D:\\temp\\ocr\\test55.png"));
                ImageIO.write(resultImage, "png", new File(newImagePath));
                return ImageMerger(originalImagePath, newImagePath, imgStorage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch ( Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 合并两张图片
     * @param originalImagePath
     * @param newImagePath
     */
    public String ImageMerger(String originalImagePath, String newImagePath,  String imgStorage) {
        try {
            // 加载两张图片
            BufferedImage image1 = ImageIO.read(new File(originalImagePath));
            BufferedImage image2 = ImageIO.read(new File(newImagePath));

            // 计算新图片的宽度和高度
            int newWidth = image1.getWidth() + image2.getWidth();
            int newHeight = Math.max(image1.getHeight(), image2.getHeight());

            // 创建一个新的BufferedImage来容纳合并后的图片
            BufferedImage mergedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            // 获取Graphics2D对象以在新图片上绘制
            Graphics2D g2d = mergedImage.createGraphics();

            // 设置背景色为白色（可选，如果你想要一个透明的背景，则不需要这一步）
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, newWidth, newHeight);

            // 绘制第一张图片
            g2d.drawImage(image1, 0, 0, null);

            // 绘制第二张图片，放在第一张图片的右边
            g2d.drawImage(image2, image1.getWidth(), 0, null);

            // 释放Graphics2D对象
            g2d.dispose();

            // 将合并后的图片保存到文件
            String imgName = System.nanoTime() + ".png";
            ImageIO.write(mergedImage, "jpg", new File(imgStorage + imgName));



//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(mergedImage, "jpg", baos);
//            baos.flush();
//
//            // 获取图像数据的字节数组
//            byte[] imageBytes = baos.toByteArray();
//            baos.close();
//
//            // 将字节数组编码为Base64字符串
//            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            System.out.println("Images merged successfully!");
            return imgName;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
