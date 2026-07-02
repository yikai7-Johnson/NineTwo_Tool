package com.ninetwo.boot.util;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

public class OCRClient {
    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost("http://localhost:5000/ocr");

        File file = new File("C:\\Users\\chaoz\\Desktop\\22.jpg");
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                .build();

        uploadFile.setEntity(reqEntity);

        try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                System.out.println(EntityUtils.toString(resEntity));
            }
            EntityUtils.consume(resEntity);
        } finally {
            httpClient.close();
        }
    }
}