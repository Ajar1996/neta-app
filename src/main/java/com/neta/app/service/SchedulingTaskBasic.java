package com.neta.app.service;

import cn.hutool.core.util.RandomUtil;
import com.neta.app.model.NetaResponse;
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

    /**
     * 每天8点执行一次
     */
    @Scheduled(cron = "0 0 8 * * ?")
    //@Scheduled(cron = "*/5 * * * * ?")
    private void printNowDate() throws InterruptedException {
        List<NetaResponse> netaResponses = requestService.getArticleList();
        for (NetaResponse netaResponse : netaResponses) {
            //休眠，避免被发现是脚本
            Thread.sleep(RandomUtil.randomInt(5000, 15000));
            requestService.insertArtComment(netaResponse.getOpenId(), netaResponse.getGroupId());
            Thread.sleep(RandomUtil.randomInt(1000, 3000));
            requestService.forwarArticle(netaResponse.getGroupId());
        }
        requestService.sign();
        log.info("执行成功");
    }
}
