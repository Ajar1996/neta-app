package com.neta.app.service;

import cn.hutool.core.util.RandomUtil;
import com.neta.app.entity.User;
import com.neta.app.model.NetaResponse;
import com.neta.app.model.Token;
import com.neta.app.model.TokenConfiguration;
import com.neta.app.service.impl.MailService;
import com.neta.app.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 17:44
 */
@Component
@Slf4j
public class SchedulingTaskBasic {


    @Resource
    RequestService requestService;

    @Resource
    TokenConfiguration tokenConfiguration;

    @Resource
    UserServiceImpl userService;

    @Resource
    MailService mailService;
    /**
     * 每天1点执行一次
     */
    // @Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(cron = "*/5 * * * * ?")
    private void sign() throws InterruptedException {

        List<User> userList = userService.list();
        for (User user : userList) {
            try {
                log.info("{}开始执行", user.getName());
                Thread.sleep(RandomUtil.randomInt(12000, 20000));
                Token token = requestService.refreshToken(user.getRefreshToken());
                if (token == null) {
                    continue;
                }
                user.setRefreshToken(token.getRefreshToken());
                //更新token

                userService.updateById(user);


                String authorization = token.getAuthorization();
                List<NetaResponse> netaResponses = requestService.getArticleList(authorization);
                for (NetaResponse netaResponse : netaResponses) {
                    //休眠，避免被发现是脚本
                    Thread.sleep(RandomUtil.randomInt(10000, 15000));
                    requestService.insertArtComment(netaResponse.getOpenId(), netaResponse.getGroupId(), authorization);
                    Thread.sleep(RandomUtil.randomInt(10000, 15000));
                    requestService.forwarArticle(netaResponse.getGroupId(), authorization);
                }
                requestService.sign(authorization);
                log.info("{}执行成功", user.getName());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}执行失败", user.getName());
                mailService.sendSimpleMail(user.getEmail(), "哪吒APP签到失败", "请登录网站刷新你的授权码");
            }

        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    private void checkSign() throws InterruptedException {
        List<User> userList = userService.list();
        for (User user : userList) {

            try {
                requestService.checkSign(user.getRefreshToken());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}还没有签到", user.getName());
                mailService.sendSimpleMail(user.getEmail(), "哪吒APP签到提醒", "你今天的app还没有签到，请检查");
            }
        }
    }

}
