package com.neta.app.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.neta.app.emnu.RequestEnum;
import com.neta.app.emnu.commentsEnum;
import com.neta.app.model.NetaResponse;
import com.neta.app.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 17:31
 */
@Service
@Slf4j
public class RequestServiceImpl implements RequestService {
    @Value("${authorization}")
    String authorization;

    @Value("${refreshToken}")
    String refreshToken;

    @Override
    public int forwarArticle(String groupId) {
        //转发帖子
        String forwar = HttpRequest.put(RequestEnum.forwarArticle.getUrl())
                .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                .body("{\"articleId\":\"" +
                        groupId +
                        "\",\"forwardTo\":\"1\"}")//表单内容
                .timeout(20000)//超时，毫秒
                .execute().body();
        if ((Integer) JSONUtil.parseObj(forwar).get("code") != 200) {
            log.error("转发失败，{}", forwar);
            //发邮件提醒
            System.exit(0);
        }

        log.info("转发成功,{}", forwar);
        return (Integer) JSONUtil.parseObj(forwar).get("code");
    }

    @Override
    public int insertArtComment(String openId, String groupId) {
        //评论帖子
        String commnetsRespone = HttpRequest.post(RequestEnum.insertArtComment.getUrl())
                .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                .body("{\"content\":\"" +
                        commentsEnum.valueOf("comments" + RandomUtil.randomInt(1, 10))
                        + "\",\"parentId\":null,\"openId\":\"" +
                        openId +
                        "\",\"groupId\":\"" +
                        groupId +
                        "\",\"generateType\":\"ugc_api\"}")//表单内容
                .timeout(20000)//超时，毫秒
                .execute().body();

        if ((Integer) JSONUtil.parseObj(commnetsRespone).get("code") != 200) {
            log.error("评论失败，{}", commnetsRespone);
            //发邮件提醒
            System.exit(0);
        }
        log.info("评论成功,{}", commnetsRespone);

        return (Integer) JSONUtil.parseObj(commnetsRespone).get("code");
    }

    @Override
    public List<NetaResponse> getArticleList() {
        //获取帖子列表
        String articleListResponse = HttpRequest.get(RequestEnum.getArticleList.getUrl())
                .header(Header.AUTHORIZATION, authorization)
                .form("category", "xiaoquan")
                .form("refreshType", "refresh")
                .form("uuid", IdUtil.simpleUUID())
                .timeout(20000)//超  时，毫秒
                .execute().body();

        //token过期
        if ((Integer) JSONUtil.parseObj(articleListResponse).get("code") == 416) {
            boolean success = this.refreshToken();
            if (success) {
                //获取帖子列表
                articleListResponse = HttpRequest.get(RequestEnum.getArticleList.getUrl())
                        .header(Header.AUTHORIZATION, authorization)
                        .form("category", "xiaoquan")
                        .form("refreshType", "refresh")
                        .form("uuid", IdUtil.simpleUUID())
                        .timeout(20000)//超  时，毫秒
                        .execute().body();
            }
        }

        if ((Integer) JSONUtil.parseObj(articleListResponse).get("code") != 200) {
            log.error("获取帖子列表失败，{}", articleListResponse);
            //发邮件提醒
            System.exit(0);
        }

        List<NetaResponse> netaResponse = new ArrayList<>();

        for (int i = 2; i < 6; i++) {
            JSONObject articleListJson = JSONUtil.parseObj(articleListResponse);
            JSONArray jsonArray = (JSONArray) articleListJson.get("data");
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONObject article = (JSONObject) jsonObject.get("article");
            String openId = (String) article.get("openId");
            String groupId = (String) article.get("groupId");
            netaResponse.add(NetaResponse.builder().groupId(groupId).openId(openId).build());
        }


        return netaResponse;
    }

    @Override
    public int sign() {
        //签到
        String sign = HttpRequest.get(RequestEnum.sign.getUrl())
                .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                .timeout(20000)//超时，毫秒
                .execute().body();
        if ((Integer) JSONUtil.parseObj(sign).get("code") != 200) {
            log.error("签到失败信息为，{}", sign);
            //发邮件提醒
            System.exit(0);
        }

        return (Integer) JSONUtil.parseObj(sign).get("code");
    }


    @Override
    public boolean refreshToken() {
        //刷新token
        String tokenResponse = HttpRequest.post(RequestEnum.refreshToken.getUrl())
                .form("refreshToken", refreshToken)
                .timeout(20000)//超时，毫秒
                .execute().body();

        if ((Integer) JSONUtil.parseObj(tokenResponse).get("code") != 20000) {
            log.error("刷新失败,{}", tokenResponse);

            //发邮件，刷新失败

            //退出程序
            System.exit(0);
        }

        this.refreshToken = (String) JSONUtil.parseObj(JSONUtil.parseObj(tokenResponse).get("data")).get("refresh_token");
        this.authorization = (String) JSONUtil.parseObj(JSONUtil.parseObj(tokenResponse).get("data")).get("access_token");
        log.info("token刷新成功,authorization为{}，refreshToken为{}", this.authorization, this.refreshToken);
        return true;
    }
}
