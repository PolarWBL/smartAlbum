package com.example.smartalbum.service;

import com.example.smartalbum.domain.Depository;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.exception.FileOperationException;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.util.ResponseMsgUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * 更新照片状态, StateId = 3 表示处于回收状态
 * @author Boliang Weng
 */
@Service
public class RubbishService {
    @Resource
    private ImageDataService imageDataService;


    @Transactional(rollbackFor = Exception.class)
    public boolean setImageToRubbish(String imageName, HttpSession session){
        User userInfo = (User) session.getAttribute("userInfo");
        Depository depository = userInfo.getDepository();
        int depositoryId = depository.getId();

        //更新照片状态
        Image image = new Image();
        image.setStateId(3);

        int imageId = imageDataService.getImageId(imageName, depositoryId);
        return imageDataService.updateSelective(image, imageId) > 0;
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean setImageToNormal(String imageName, HttpSession session){
        User userInfo = (User) session.getAttribute("userInfo");
        Depository depository = userInfo.getDepository();
        int depositoryId = depository.getId();

        //更新照片状态
        Image image = new Image();
        image.setStateId(1);

        int imageId = imageDataService.getImageId(imageName, depositoryId);
        return imageDataService.updateSelective(image, imageId) > 0;
    }



}
