package com.example.smartalbum.controller;

import com.example.smartalbum.exception.EmailFormatException;
import com.example.smartalbum.service.MailSenderService;
import com.example.smartalbum.service.MailService;
import com.example.smartalbum.service.UserService;
import com.example.smartalbum.util.MD5Utils;
import com.example.smartalbum.util.ResponseMsgUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户注册
 *
 * @author Administrator
 */
@Controller
public class RegisterController {

    @Resource
    private UserService userService;
    @Resource
    private MailService mailService;
    @Resource
    private MailSenderService mailSenderService;

    /**
     * 发送邮箱验证码
     *
     * @param mail 收件邮箱
     */
    @ResponseBody
    @GetMapping("/sendcode")
    public String sendEmailCode(@RequestParam("mail") String mail) {
        if (mail == null) {
            return ResponseMsgUtil.failure("请输入邮箱");
        }
        if (!mailService.checkMailFormat(mail)) {
            return ResponseMsgUtil.failure("邮箱格式错误");
        }
        mailSenderService.sendEmailCode(mail);
        return ResponseMsgUtil.success("发送验证码成功！");
    }

    /**
     * 注册
     *
     * @param username 用户名
     * @param password 密码
     * @param mail     电子邮箱
     * @param code     邮箱验证码
     */
    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("mail") String mail,
                           @RequestParam("code") String code,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {

        if (userService.hasMail(mail, true)) {
            throw new EmailFormatException("邮件格式错误或已被使用");
        }
        //检测验证码后，再把验证码删了
        if (mailService.hasEmailCode(mail, code) && mailService.delEmailCode(mail)) {
            //注册成功后进行登录操作
            String md5Password = MD5Utils.getMd5String(password);
            if (userService.register(username, md5Password, mail)) {
                request.getRequestDispatcher("/login").forward(request, response);
            }
        }
        return "index";
    }
}
