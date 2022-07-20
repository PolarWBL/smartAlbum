package com.example.smartalbum.service;

import com.example.smartalbum.domain.Depository;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.service.database.DepositoryDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 数据更新服务
 *
 * @author Administrator
 */
@Slf4j
@Service
public class UpdateService {
    @Resource
    private OssService ossService;
    @Resource
    private DepositoryDataService depositoryDataService;

    /**
     * 更新数据库数据（depository的size）和 session中的数据
     *
     * @param session HttpSession
     */
    public void updateUserInfo(HttpSession session) {
        User userInfo = (User) session.getAttribute("userInfo");

        Depository depository = updateUserInfoUtil(userInfo);

        //更新session中的userInfo
        userInfo.setDepository(depository);
        session.setAttribute("userInfo", userInfo);
    }

    public Depository updateUserInfoUtil(User user) {
        String depositoryName = user.getDepository().getName();

        //云端size
        long size = ossService.getDepositorySize(depositoryName);

        Depository depository = new Depository();
        depository.setId(user.getDepository().getId());
        depository.setName(user.getDepository().getName());
        depository.setSize(size + "");
        depository.setSizeMax(user.getDepository().getSizeMax());

        //更新数据库中的size
        if (depositoryDataService.updateSelective(depository, depositoryName) <= 0 ){
            log.error("更新id为{}仓库的空间占用情况失败", depository.getId());
        }

        return depository;
    }
}
