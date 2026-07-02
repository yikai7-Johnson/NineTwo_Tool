package com.ninetwo.boot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import java.io.File;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String source, String to, String subject, String content) {
        MimeMessagePreparator preparator = mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));
            mimeMessage.setFrom(new InternetAddress(source));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(content);
        };

        try {
            mailSender.send(preparator);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }

    public void sendMailFile(String source , String to, String subject, String content) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setFrom(source);
            helper.setSubject(subject);
            helper.setText("<h1>" + content + "</h1>", true); // 第二个参数为 true 表示 HTML 内容

            File file1 = new File("/Users/zhang/file/msg/1753429034815.pdf");
            File file2 = new File("/Users/zhang/file/msg/1753429002194.pdf");
            // 添加多个附件
            helper.addAttachment("附件1.txt", file1);
            helper.addAttachment("附件2.jpg", file2);
        };
        try {
            mailSender.send(preparator);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
