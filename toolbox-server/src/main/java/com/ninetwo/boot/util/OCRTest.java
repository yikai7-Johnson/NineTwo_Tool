package com.ninetwo.boot.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OCRTest {
    public static void main2(String[] args) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("D:\\dev\\python\\python39\\python.exe", "C:\\Users\\chaoz\\Desktop\\abc\\ocr.py", "C:\\Users\\chaoz\\Desktop\\123.png");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("OCR Result:\n" + result.toString());
            } else {
                System.out.println("Error executing Python script");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            String[] cmd = {
                    "D:\\dev\\python\\python39\\python.exe",
                    "C:\\Users\\chaoz\\Desktop\\abc\\ocr.py",
                    "C:\\Users\\chaoz\\Desktop\\123.png"
            };
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

