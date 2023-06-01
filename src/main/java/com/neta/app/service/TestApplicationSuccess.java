package com.neta.app.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.neta.app.emnu.RequestEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/6/1 15:54
 */
@Component
@Slf4j
public class TestApplicationSuccess implements ApplicationRunner {
    @Resource
    RequestService requestService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //获取帖子列表
        String articleListResponse = HttpRequest.get(RequestEnum.getArticleList.getUrl())
                .header(Header.AUTHORIZATION, requestService.getToken())
                .form("category", "xiaoquan")
                .form("refreshType", "refresh")
                .form("uuid", IdUtil.simpleUUID())
                .timeout(20000)//超  时，毫秒
                .execute().body();

        //token过期
        if ((Integer) JSONUtil.parseObj(articleListResponse).get("code") == 416) {
            boolean success = requestService.refreshToken();
            if (success) {
                //获取帖子列表
                articleListResponse = HttpRequest.get(RequestEnum.getArticleList.getUrl())
                        .header(Header.AUTHORIZATION, requestService.getToken())
                        .form("category", "xiaoquan")
                        .form("refreshType", "refresh")
                        .form("uuid", IdUtil.simpleUUID())
                        .timeout(20000)//超  时，毫秒
                        .execute().body();
            }
        }

        if ((Integer) JSONUtil.parseObj(articleListResponse).get("code") != 200) {
            log.error("程序启动失败，请检查token，{}", articleListResponse);
            System.exit(0);
        } else {
            log.info("程序验证成功！拿铁加油！");
        }
    }
}
