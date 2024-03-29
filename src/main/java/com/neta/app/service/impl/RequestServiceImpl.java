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
import com.neta.app.model.Token;
import com.neta.app.service.RequestService;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public int forwarArticle(String groupId, String authorization) throws Exception {
        //转发帖子
        String forwar =
                HttpRequest.put(RequestEnum.forwarArticle.getUrl())
                        .header(Header.CONTENT_TYPE, "application/json;charset=utf-8")
                        .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                        .body("{\"articleId\":\"" + RandomUtil.randomInt(10) + "\",\"forwardTo\":\"1\"}")//表单内容
                        .timeout(40000)//超时，毫秒
                        .execute().body();
        if ((Integer) JSONUtil.parseObj(forwar).get("code") != 200) {
            log.error("转发失败，{}", forwar);
            throw new Exception("转发失败" + forwar);
        }

        log.info("转发成功,{}", forwar);
        return (Integer) JSONUtil.parseObj(forwar).get("code");
    }

    @Override
    public int insertArtComment(String openId, String groupId, String authorization) throws Exception {
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
                .timeout(40000)//超时，毫秒
                .execute().body();

        if ((Integer) JSONUtil.parseObj(commnetsRespone).get("code") != 200) {
            log.error("评论失败，{}", commnetsRespone);
            throw new Exception("评论失败"+commnetsRespone);
        }
        log.info("评论成功,{}", commnetsRespone);

        return (Integer) JSONUtil.parseObj(commnetsRespone).get("code");
    }

    @Override
    public List<NetaResponse> getArticleList(String authorization) throws Exception {
        //获取帖子列表
        String articleListResponse = HttpRequest.get(RequestEnum.getArticleList.getUrl())
                .header(Header.AUTHORIZATION, authorization)
                .form("category", "xiaoquan")
                .form("refreshType", "refresh")
                .form("uuid", IdUtil.simpleUUID())
                .timeout(40000)//超  时，毫秒
                .execute().body();


        if ((Integer) JSONUtil.parseObj(articleListResponse).get("code") != 200) {
            log.error("获取帖子列表失败，{}", articleListResponse);
            throw new Exception("获取帖子列表失败"+articleListResponse);
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
    public int sign(String authorization) throws Exception {
        //签到
        String sign = HttpRequest.get(RequestEnum.sign.getUrl())
                .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                .timeout(40000)//超时，毫秒
                .execute().body();

        if ((Integer) JSONUtil.parseObj(sign).get("code") == 417) {
            log.error("签到失败信息为，{}", sign);
            return (Integer) JSONUtil.parseObj(sign).get("code");
        }

        if ((Integer) JSONUtil.parseObj(sign).get("code") != 200) {
            log.error("签到失败信息为，{}", sign);
            throw new Exception("签到失败信息"+sign);
        }

        return (Integer) JSONUtil.parseObj(sign).get("code");
    }


    @Override
    public Token refreshToken(String refreshToken) throws Exception {
        //刷新token
        String tokenResponse = HttpRequest.post(RequestEnum.refreshToken.getUrl())
                .form("refreshToken", refreshToken)
                .timeout(40000)//超时，毫秒
                .execute().body();

        if ((Integer) JSONUtil.parseObj(tokenResponse).get("code") != 20000) {
            log.error("刷新失败,{}", tokenResponse);
            throw new Exception("tokenResponse");
        }
        Token token = new Token();
        token.setRefreshToken((String) JSONUtil.parseObj(JSONUtil.parseObj(tokenResponse).get("data")).get("refresh_token"));
        token.setAuthorization((String) JSONUtil.parseObj(JSONUtil.parseObj(tokenResponse).get("data")).get("access_token"));
        log.info("token刷新成功,refreshToken为");
        log.info(token.getRefreshToken());
        return token;
    }

    @Override
    public boolean checkSign(String authorization) throws Exception {
        //检查是否签到
        String checkSignResponse = HttpRequest.get(RequestEnum.checkSign.getUrl())
                .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                .timeout(40000)//超时，毫秒
                .execute().body();
        Integer checkSign = ((Integer) JSONUtil.parseObj(JSONUtil.parseObj(checkSignResponse).get("data")).get("sign"));
        if (checkSign!=null&&checkSign == 0) {
            throw new Exception("还没签到");
        } else
            return true;

    }


}
