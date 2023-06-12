package com.neta.app.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.neta.app.emnu.RequestEnum;
import com.neta.app.model.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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

    @Value("${refreshToken}")
    List<String> refreshToken;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        for (int i = 0; i < refreshToken.size(); i++) {
            Token token = requestService.refreshToken(refreshToken.get(i));
            if (token == null) {
                log.error("token验证失败，请检查token，{}", refreshToken.get(i));
                break;
            }
            //获取帖子列表
            String articleListResponse = HttpRequest.get(RequestEnum.getArticleList.getUrl())
                    .header(Header.AUTHORIZATION, token.getAuthorization())
                    .form("category", "xiaoquan")
                    .form("refreshType", "refresh")
                    .form("uuid", IdUtil.simpleUUID())
                    .timeout(20000)//超  时，毫秒
                    .execute().body();
        }
        log.info("程序验证成功！拿铁加油！");
    }
}
