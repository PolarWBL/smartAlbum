package com.example.smartalbum.scheduled;

import com.ecloud.sdk.vcr.VcrClient;
import com.ecloud.sdk.vcr.model.ModerationResultBody;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.util.ImageCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Boliang Weng
 */
@Component
@Slf4j
public class ImageCheckScheduled {
    private static List<ModerationResultBody> resultBodies;
    @Resource
    private ImageDataService imageDataService;
    @Resource
    private VcrClient vcrClient;

    @Scheduled(initialDelay = 1000 * 60 * 3, fixedRate = 1000 * 60 * 60 * 2)
    public void submitCheck() {
        resultBodies.clear();
        List<Image> imageList = imageDataService.getNormalImages();
        if (imageList.isEmpty()) {
            log.info("未发现新的未审核图片");
            return;
        }
        if (imageList.size() > 90) {
            imageList = imageList.subList(0, 80);
        }
        ImageCheckUtil imageCheckUtil = new ImageCheckUtil();
        resultBodies = imageCheckUtil.asyncCheck(imageList, vcrClient);
    }

    @Scheduled(initialDelay = 1000 * 60 * 33, fixedRate = 1000 * 60 * 60 * 2)
    public void resultCheck(){
        int count = 0;
        int red = 0;
        if (resultBodies.isEmpty()) {
            log.info("还未提交新的审核数据, 无需进行更新操作");
            return;
        }
        ImageCheckUtil imageCheckUtil = new ImageCheckUtil();
        List<ModerationResultBody> result = imageCheckUtil.getResult(resultBodies, vcrClient);
        for (ModerationResultBody resultBody : result) {
            int imageId = Integer.parseInt(resultBody.getDataId());
            int stateId = resultBody.getLabels().get(0).getLabel();
            int res;
            if (stateId != 200100) {
                res = imageDataService.updateAfterCensor(imageId, stateId);
                log.info("id为 {} 的图片的违规代码为: {}", imageId, stateId);
                red++;
            }else {
                res = imageDataService.updateAfterCensor(imageId, 0);
            }
            if (res > 0) {
                count += res;
            }else {
                log.error("更新id为{}的图片信息时失败!", imageId);
            }
        }
        log.info("成功自动审查了{}张图片, 其中包括{}张疑似违规图片", count, red);
        resultBodies.clear();
    }

}
