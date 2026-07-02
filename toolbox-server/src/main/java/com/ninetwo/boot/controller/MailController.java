package com.ninetwo.boot.controller;

import com.alibaba.fastjson.JSON;
import com.ninetwo.boot.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/MailTo")
public class MailController {

    @Autowired
    private MailService mailService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @PostMapping("/sendMail")
    public ResponseEntity<String> sendMail(@RequestBody Map<String, String> body) {
        try {
            String to = body.get("to");
            String subject = body.get("subject");
            String content = body.get("content");

            System.out.println("Sending mail from: " + fromEmail + " to: " + to);
            System.out.println("Subject: " + subject);

            mailService.sendMail(fromEmail, to, subject, content);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Mail sent successfully");
            return ResponseEntity.ok(JSON.toJSONString(res));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(JSON.toJSONString(res));
        }
    }
}
