package com.example.smartalbum.controller;

import com.example.smartalbum.service.UserService;
import com.example.smartalbum.util.MD5Utils;
import com.example.smartalbum.util.ResponseMsgUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户重置密码
 *
 * @author Administrator
 */
@Controller
public class ResetPasswordController {

    @Resource
    private UserService userService;

    /**
     * 重置密码
     *
     * @param username 用户名
     * @param password 密码
     * @param mail     电子邮箱
     * @param code     邮箱验证码
     */
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("mail") String mail,
                           @RequestParam("code") String code,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException, ServletException {
        System.out.println("================重置密码==============");
        if (!userService.resetPassword(username, password, mail, code)) {
            throw new RuntimeException("密码重置失败!");
        } else {
            request.getRequestDispatcher("/login").forward(request, response);
        }

        return "index";
    }


}
