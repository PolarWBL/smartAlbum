package com.example.smartalbum;

import com.ecloud.sdk.common.auth.Credential;
import com.ecloud.sdk.common.http.HttpConfig;
import com.ecloud.sdk.vcr.VcrClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class SmartAlbumApplicationTests {


    private static String ecloudAccessKey;
    private static String ecloudSecretKey;

    @Value("${ecloud.ecloudAccessKey}")
    public void setEcloudAccessKey(String ecloudAccessKey) {
        SmartAlbumApplicationTests.ecloudAccessKey = ecloudAccessKey;
    }

    @Value("${ecloud.ecloudSecretKey}")
    public void setEcloudSecretKey(String ecloudSecretKey) {
        SmartAlbumApplicationTests.ecloudSecretKey = ecloudSecretKey;
    }

    public static VcrClient getClient() {
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setTimeout(5);
        Credential credential = new Credential(ecloudAccessKey, ecloudSecretKey);
        return new VcrClient(credential, httpConfig);
    }


    @Test
    void test01() throws IOException {
        System.out.println("=======================> 输出 <=========================");
        System.out.println(ecloudAccessKey);
        System.out.println(ecloudSecretKey);
        System.out.println("=======================================================");
    }

}


