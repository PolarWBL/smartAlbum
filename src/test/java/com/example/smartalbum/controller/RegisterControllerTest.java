package com.example.smartalbum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RegisterControllerTest {

    @Resource
    private RegisterController registerController;

    @Test
    void sendEmailCode() {
        List<String> mailList = Arrays.asList(null, "", "111", "5555124@gmail.com", "wengboliang0099@qq.com");

        for (String mail : mailList) {
            try {
                System.out.println("====" + registerController.sendEmailCode(mail) + "====");
            } catch (Exception e) {
                System.out.println("====" + e + "====");
            }
        }

    }
}