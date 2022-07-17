package com.example.smartalbum.controller.page;

import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.service.database.ImageDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.example.smartalbum.controller.page.UserMainController.putUserinfoIntoTheModel;

/**
 * @author Boliang Weng
 */
@Controller
public class RecycleBinController {
    @Resource
    private ImageDataService imageDataService;

    @GetMapping("/recycleBin")
    public String userMain(Model model, HttpSession session) {
        //仓库id
        User user = (User) session.getAttribute("userInfo");

        if (user != null) {
            List<Image> fileList = imageDataService.getRecycleImages(user.getDepository().getId());
            //把用户信息放入model
            putUserinfoIntoTheModel(model, session);
            model.addAttribute("fileList", fileList);
            return "recycleBin";
        }
        return "index";
    }


}
