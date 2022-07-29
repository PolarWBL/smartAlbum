package com.example.smartalbum.controller;

import com.ecloud.sdk.common.http.ECloudHttpResponse;
import com.ecloud.sdk.vcr.VcrClient;
import com.ecloud.sdk.vcr.model.ImageModerationReq;
import com.ecloud.sdk.vcr.model.ModerationResultBody;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.scheduled.ImageCheckScheduled;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.util.ResponseMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Boliang Weng
 */
@Controller
@Slf4j
@RequestMapping("/imageCheck")
public class ImageCheckController {

    @Resource
    private ImageDataService imageDataService;
    @Resource
    private VcrClient vcrClient;
    @Resource
    private ImageCheckScheduled imageCheckScheduled;

    @ResponseBody
    @PostMapping("/check")

    public String getResults(HttpSession session) {
        log.info("开始手动提交图片审核请求");
        User user = (User) session.getAttribute("userInfo");
        if (user == null) {
            return ResponseMsgUtil.failure("用户不存在!");
        } else if (user.getDepository().getId() != 0) {
            return ResponseMsgUtil.failure("您不是管理员!");
        } else if (imageCheckScheduled.isChecking()) {
            return ResponseMsgUtil.failure("系统正在检测中, 请稍后再试...");
        }

        List<Image> imageList = imageDataService.getNormalImages();
        if (imageList.isEmpty()) {
            log.info("未发现新的未审核图片");
            return ResponseMsgUtil.success("当前全部图片均已检测完毕, 无需拉取!");
        }
        if (imageList.size() > 20) {
            imageList = imageList.subList(0, 10);
        }
        log.info("找到了{}张未审核的图片", imageList.size());
        List<ImageModerationReq> imageModerationReqs = new ArrayList<>();
        for (Image image : imageList) {
            ImageModerationReq imageModerationReq = new ImageModerationReq();
            imageModerationReq.setUrl(image.getUrlMini());
            imageModerationReq.setDataId(image.getId().toString());
            imageModerationReq.setScenesId("71op0vbok0wy");
            imageModerationReq.setCheckGc("1");
            imageModerationReqs.add(imageModerationReq);
        }
        return ResponseMsgUtil.success(updateImages(imageModerationReqs));
    }

    private String updateImages(List<ImageModerationReq> imageModerationReqs) {
        log.info("开始提交检测...");
        int count = 0;
        int red = 0;
        for (ImageModerationReq imageModerationReq : imageModerationReqs) {
            ECloudHttpResponse<ModerationResultBody> moderationResultBody =
                    vcrClient.imageCensor(imageModerationReq);
            ModerationResultBody resultBody = moderationResultBody.getBody();

            if ("0".equals(moderationResultBody.getErrorCode())){
                int imageId = Integer.parseInt(resultBody.getDataId());
                int stateId = resultBody.getLabels().get(0).getLabel();
                int res;
                if (stateId != 200100) {
                    res = imageDataService.updateAfterCensor(imageId, stateId);
                    log.info("id为 {} 的图片的违规代码为: {}", imageId, stateId);
                    red++;
                } else {
                    res = imageDataService.updateAfterCensor(imageId, 0);
                }
                if (res > 0) {
                    count += res;
                } else {
                    log.error("更新id为{}的图片信息时失败!", imageId);
                }
            }else {
                log.error("errorCode:{}, errorMessage:{}", moderationResultBody.getErrorCode(), moderationResultBody.getErrorMessage());
            }
        }
        String msg = "本次检测了[" + count + "]张图片, 新增[" + red + "]张疑似违规图片";
        log.info(msg);
        return msg;
    }

}
