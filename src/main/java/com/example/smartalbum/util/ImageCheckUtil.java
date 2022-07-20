package com.example.smartalbum.util;

import com.ecloud.sdk.common.auth.Credential;
import com.ecloud.sdk.common.http.ECloudHttpResponse;
import com.ecloud.sdk.common.http.HttpConfig;
import com.ecloud.sdk.common.utils.JsonUtils;
import com.ecloud.sdk.vcr.VcrClient;
import com.ecloud.sdk.vcr.model.ImageBatchModel;
import com.ecloud.sdk.vcr.model.ImageBatchModerationReq;
import com.ecloud.sdk.vcr.model.ModerationQueryReq;
import com.ecloud.sdk.vcr.model.ModerationResultBody;
import com.example.smartalbum.domain.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Boliang Weng
 */
@Slf4j
@Component
public class ImageCheckUtil {
    public List<ModerationResultBody> asyncCheck(List<Image> imageList, VcrClient client){
        log.info("开始提交图片审核");
        List<ImageBatchModel> modelList = new ArrayList<>();

        for (Image image : imageList) {
            ImageBatchModel model = new ImageBatchModel();
            model.setUrl(image.getUrlMini());
            model.setDataId(image.getId().toString());
            modelList.add(model);
//            log.info("封装图片:{}", model);
        }

        ImageBatchModerationReq req = new ImageBatchModerationReq();
        req.setCallBack("www.callback.com");
        req.setScenesId("");
        req.setUrls(modelList);
        ECloudHttpResponse<List<ModerationResultBody>> listBotHttpResponse = client.imageBatchCensor(req);
        if (!"0".equals(listBotHttpResponse.getErrorCode())) {
            log.error("图片审核提交失败! {}",JsonUtils.toJSON(listBotHttpResponse));
            throw new RuntimeException("图片审核提交失败!");
        }
        log.info("图片审核提交成功!");
        return listBotHttpResponse.getBody();

    }



    public List<ModerationResultBody> getResult(List<ModerationResultBody> bodies, VcrClient client){
        log.info("开始获取图片审核结果");
        ModerationQueryReq req = new ModerationQueryReq();
        List<String> taskIds = new ArrayList<>();

        for (ModerationResultBody body : bodies) {
            taskIds.add(body.getTaskId());
        }

        req.setTaskIds(taskIds);
        ECloudHttpResponse<List<ModerationResultBody>> moderationResultBody = client.imageCheckResults(req);
        if (!"0".equals(moderationResultBody.getErrorCode())) {
            log.error("图片审核信息获取失败! {}", JsonUtils.toJSON(moderationResultBody));
            throw new RuntimeException("图片审核信息获取失败!");
        }
        log.info("图片审核信息获取成功!");
        return moderationResultBody.getBody();
    }



}
