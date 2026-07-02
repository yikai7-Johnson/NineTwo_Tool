package com.ninetwo.boot.util;



import net.coobird.thumbnailator.Thumbnails;




import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;

import java.io.IOException;

import java.io.InputStream;

import java.io.OutputStream;

import java.net.URLEncoder;



public class ImageCompressor {



    /**

     * 压缩图片文件大小

     *

     * @param inputStream  输入图片流

     * @param outputStream 输出图片流

     * @param quality      压缩质量 (0.0 到 1.0，1.0表示最高质量)

     * @throws IOException

     */

    public static void compressImage(InputStream inputStream, ByteArrayOutputStream outputStream, int width, int height, double quality, String imgSuffix) throws IOException {

        // 使用Thumbnailator进行压缩

        Thumbnails.of(inputStream)

                .size(width, height)

                .outputQuality(quality)

                .outputFormat(imgSuffix)

                .toOutputStream(outputStream);

    }



}

