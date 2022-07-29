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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Boliang Weng
 */
@Component
@Slf4j
public class ImageCheckScheduled {
    private static List<ModerationResultBody> resultBodies;
    private int sign;
    @Resource
    private ImageDataService imageDataService;
    @Resource
    private VcrClient vcrClient;


    @Scheduled(initialDelay = 1000 * 60 * 10, fixedRate = 1000 * 60 * 60 * 2)
    public void submitCheck() {
        if (resultBodies != null && !resultBodies.isEmpty()) {
            log.info("结果集还未处理完毕, 正在等待处理...");
            sign = 1;
            return;
        }
        sign = 1;
        List<Image> imageList = imageDataService.getNormalImages();
//        log.info(imageList.toString());
        if (imageList.isEmpty()) {
            log.info("未发现新的未审核图片");
            sign = 0;
            return;
        }
        if (imageList.size() > 90) {
            imageList = imageList.subList(0, 80);
        }
        ImageCheckUtil imageCheckUtil = new ImageCheckUtil();
        resultBodies = imageCheckUtil.asyncCheck(imageList, vcrClient);
    }

    @Scheduled(initialDelay = 1000 * 60 * 15, fixedRate = 1000 * 60 * 60 * 2)
    public void resultCheck(){
        int count = 0;
        int red = 0;
        if (resultBodies == null || resultBodies.isEmpty()) {
            log.info("还未提交新的审核数据, 无需获取新的响应集!");
            sign = 0;
            return;
        }
        ImageCheckUtil imageCheckUtil = new ImageCheckUtil();
        List<ModerationResultBody> result = imageCheckUtil.getResult(resultBodies, vcrClient);
        List<ModerationResultBody> uncheckedImages = new ArrayList<>();
        int index = 0;
        for (ModerationResultBody resultBody : result) {
            int imageId = Integer.parseInt(resultBody.getDataId());
            int stateId = resultBody.getLabels().get(0).getLabel();
            String status = resultBody.getStatus();

            if ("0".equals(status)) {
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
            }else {
                log.info("id为 {} 的图片正在检测中......已加入到新的查询队列", imageId);
                uncheckedImages.add(resultBodies.get(index));
            }
            index++;
        }
        log.info("共提交了{}张图片, 成功自动审查了{}张图片, 其中包括{}张疑似违规图片, 还有{}张图片正在检测中...", result.size(), count, red, uncheckedImages.size());
        resultBodies.clear();
        if (!uncheckedImages.isEmpty()) {
            resultBodies = uncheckedImages;
        }else {
            sign = 0;
        }
    }

    public boolean isChecking(){
        return sign != 0;
    }

}
