package com.neta.app.service.impl;

import cn.hutool.extra.template.TemplateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final String SENDER = "ajar1996@163.com";
    @Autowired
    private JavaMailSender mailSender;


    /**
     * 发送普通邮件
     *
     * @param to      收件人
     * @param subject 主题（标题）
     * @param content 内容
     */
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SENDER);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("发送普通邮件时发生异常!" + e);
        }
    }
}
