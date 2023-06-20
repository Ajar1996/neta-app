package com.neta.app.service;

import cn.hutool.core.util.RandomUtil;
import com.neta.app.model.NetaResponse;
import com.neta.app.model.Token;
import com.neta.app.model.TokenConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
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

    /**
     * 每天8点执行一次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "*/5 * * * * ?")
    private void printNowDate() throws InterruptedException {
        HashMap<String, String> refreshToken = tokenConfiguration.getRefreshToken();
        for (String key : refreshToken.keySet()) {
            Token token = requestService.refreshToken(refreshToken.get(key));
            if (token == null) {
                continue;
            }
            refreshToken.put(key, token.getRefreshToken());
            String authorization = token.getAuthorization();
            List<NetaResponse> netaResponses = requestService.getArticleList(authorization);
            for (NetaResponse netaResponse : netaResponses) {
                //休眠，避免被发现是脚本
                Thread.sleep(RandomUtil.randomInt(5000, 15000));
                requestService.insertArtComment(netaResponse.getOpenId(), netaResponse.getGroupId(), authorization);
                Thread.sleep(RandomUtil.randomInt(1000, 3000));
                requestService.forwarArticle(netaResponse.getGroupId(), authorization);
            }
            requestService.sign(authorization);
            log.info(key + "执行成功");
        }
    }
}
