package com.example.smartalbum.controller.page.admin;

import com.example.smartalbum.controller.file.FileController;
import com.example.smartalbum.controller.page.UserMainController;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.exception.FileOperationException;
import com.example.smartalbum.exception.UserExistsException;
import com.example.smartalbum.service.RubbishService;
import com.example.smartalbum.service.UpdateService;
import com.example.smartalbum.service.UserService;
import com.example.smartalbum.service.database.DepositoryDataService;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.service.database.UserDataService;
import com.example.smartalbum.util.MD5Utils;
import com.example.smartalbum.util.ResponseMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Administrator
 */
@Controller
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private UserService userService;
    @Resource
    private UserDataService userDataService;
    @Resource
    private ImageDataService imageDataService;
    @Resource
    private FileController fileController;
    @Resource
    private UpdateService updateService;
    @Resource
    private RubbishService rubbishService;


    @RequestMapping("/index")
    public String index() {
        return "admin/index";
    }

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession httpSession) {
        String md5Password = MD5Utils.getMd5String(password);
        log.info("========用户输入的加密后的密码:" + md5Password + "=========");
        //检测用户名和密码是否正确
        if (!userService.checkUser(username, md5Password)) {
            throw new UserExistsException("密码错误，或用户不存在！");
        }
        List<User> list = userDataService.getUserInfoByName(username);
        User user = list.get(0);
        if (user.getDepository().getId() != 0) {
            return "redirect:/userMain";
        }
        httpSession.setAttribute("userInfo", user);
        return "redirect:/admin/userMain";
    }


    @GetMapping("/userMain")
    public String userMain(Model model, HttpSession session) {
        //仓库id
        User user = (User) session.getAttribute("userInfo");

        List<Image> fileList = imageDataService.getDangerImages();

        //把用户信息放入model
        UserMainController.putUserinfoIntoTheModel(model, session);

        model.addAttribute("fileList", fileList);
        return "admin/userMain";
    }


    /**
     * 获取当前所有违规图片文件
     */
    @ResponseBody
    @GetMapping("/list")
    public String getFileList() {

        List<Image> imageList = imageDataService.getDangerImages();

        return ResponseMsgUtil.success(imageList);
    }

    /**
     * 删除(批量)违规图片
     **/
    @ResponseBody
    @PostMapping("/delete")
    public String fileDelete(@RequestParam("imagesId") String[] imagesId) {

        List<String> deleteNames = new ArrayList<>();

        for (String imageId : imagesId) {
            Image image = imageDataService.getImage(Integer.parseInt(imageId));

            Integer depositoryId = image.getDepositoryId();

            User user = userDataService.getUserInfoByDepositoryId(depositoryId);

            user = userDataService.getUserInfoByName(user.getUsername()).get(0);

            deleteNames.addAll(fileController.deleteFiles(new String[]{image.getName()}, user));

            updateService.updateUserInfoUtil(user);
        }

        return ResponseMsgUtil.success("成功删除以下文件", deleteNames);
    }


    @ResponseBody
    @PostMapping("/passFiles")
    public String passFiles(@RequestParam("imagesId") String[] imagesId) {
        if (imagesId.length <= 0) {
            throw new FileOperationException("请选择要通过的文件！");
        }
        List<String> images = new ArrayList<>();
        for (String imageId : imagesId) {
            images.add(imageId);
            boolean result = rubbishService.setImageToPass(imageId);
            if (!result){
                log.error("将 id为 {} 的图片状态设为pass时失败！",images);
            }
        }
        return ResponseMsgUtil.success("成功通过以下文件, 文件id:", images);
    }
}
