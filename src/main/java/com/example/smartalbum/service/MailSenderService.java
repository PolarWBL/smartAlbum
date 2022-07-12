package com.example.smartalbum.service;

import com.example.smartalbum.util.ResponseMsgUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Boliang Weng
 */
@Service
public class MailSenderService {
    @Resource
    private JavaMailSenderImpl javaMailSender;
    @Resource
    private MailService mailService;
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮箱验证码
     *
     * @param mail 收件邮箱
     */
    public void sendEmailCode(String mail) {
        // 生成并保存验证码
        String emailCode = mailService.getRandomMailCode();
        mailService.addEmailCode(mail, emailCode);
        // 向邮箱发送验证码
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("欢迎使用智能云相册！");
        simpleMailMessage.setText("【云尚科技】验证码为：" + emailCode+ ", 请及时验证。");
        simpleMailMessage.setTo(mail);
        simpleMailMessage.setFrom(from);
        javaMailSender.send(simpleMailMessage);
    }
}
