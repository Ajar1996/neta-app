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
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 17:31
 */
public class RequestServiceImpl implements RequestService {
    @Value("${authorization}")
    String authorization;

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
        System.out.println(forwar);
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
        System.out.println(commnetsRespone);
        return (Integer) JSONUtil.parseObj(commnetsRespone).get("code");
    }

    @Override
    public List<NetaResponse> getArticleList() {
        //获取帖子列表
        String articleListResponse = HttpRequest.get(RequestEnum.getArticleList.getUrl())
                .form("category", "xiaoquan")
                .form("refreshType", "refresh")
                .form("uuid", IdUtil.simpleUUID())
                .timeout(20000)//超  时，毫秒
                .execute().body();

        if ((Integer) JSONUtil.parseObj(articleListResponse).get("code") != 200) {
            //发邮件提醒
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
}
