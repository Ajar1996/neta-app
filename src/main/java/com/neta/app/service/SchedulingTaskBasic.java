package com.neta.app.service;

import com.neta.app.model.NetaResponse;
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
public class SchedulingTaskBasic {


    @Resource
    RequestService requestService;

    /**
     * 每天8点执行一次
     */
    @Scheduled(cron = "0 0 8 * * ?")
    //@Scheduled(cron = "*/5 * * * * ?")
    private void printNowDate() {
        List<NetaResponse> netaResponses = requestService.getArticleList();
        for (NetaResponse netaResponse : netaResponses) {
            requestService.insertArtComment(netaResponse.getOpenId(), netaResponse.getGroupId());
            requestService.forwarArticle(netaResponse.getGroupId());
        }
        requestService.sign();
        System.out.println("执行成功");
    }
}
