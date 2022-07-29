package com.example.smartalbum.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MailServiceTest {

    @Resource
    private MailService mailService;

    @Test
    void hasEmailCode() {
        try {
            mailService.hasEmailCode("wengboliang0099@qq.com", "J8K7S");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}