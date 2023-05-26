package com.neta.app;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.neta.app.emnu.RequestEnum;
import com.neta.app.emnu.commentsEnum;

import java.io.Console;


/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 13:42
 */
public class NetaApplication {
    public static void main(String[] args){

        try {
            //每天自动签到，评论，转发 获得5+2*3+1*3=14积分

            String authorization=args[0];
            //获取帖子列表
            String articleListResponse = HttpRequest.get(RequestEnum.getArticleList.getUrl())
                    .form("category","xiaoquan")
                    .form("refreshType","refresh")
                    .form("uuid", IdUtil.simpleUUID())
                    .timeout(20000)//超时，毫秒
                    .execute().body();

            if ((Integer)JSONUtil.parseObj(articleListResponse).get("code")!=200){
                //发邮件提醒
            }



            for (int i=2;i<5;i++){
                JSONObject articleListJson = JSONUtil.parseObj(articleListResponse);
                JSONArray jsonArray= (JSONArray) articleListJson.get("data");
                JSONObject jsonObject= (JSONObject) jsonArray.get(i);
                JSONObject article= (JSONObject) jsonObject.get("article");
                String openId= (String) article.get("openId");
                String groupId= (String)article.get("groupId");

                //评论帖子
                String commnetsRespone = HttpRequest.post(RequestEnum.insertArtComment.getUrl())
                        .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                        .body("{\"content\":\""+
                                commentsEnum.valueOf("comments"+ RandomUtil.randomInt(1,10))
                                +"\",\"parentId\":null,\"openId\":\"" +
                                openId+
                                "\",\"groupId\":\"" +
                                groupId+
                                "\",\"generateType\":\"ugc_api\"}")//表单内容
                        .timeout(20000)//超时，毫秒
                        .execute().body();

                Thread.sleep(RandomUtil.randomInt(1000,2000));
                //转发帖子
                String forwar = HttpRequest.put(RequestEnum.forwarArticle.getUrl())
                        .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                        .body("{\"articleId\":\"" +
                                groupId +
                                "\",\"forwardTo\":\"1\"}")//表单内容
                        .timeout(20000)//超时，毫秒
                        .execute().body();

                if ((Integer)JSONUtil.parseObj(forwar).get("code")!=200){
                    //发邮件提醒
                }
                if ((Integer) JSONUtil.parseObj(commnetsRespone).get("code")!=200){
                    //发邮件提醒
                }

                Thread.sleep(RandomUtil.randomInt(10000,20000));
            }

            Thread.sleep(RandomUtil.randomInt(10000,20000));
            //签到
            String sign = HttpRequest.get(RequestEnum.sign.getUrl())
                    .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            if ((Integer)JSONUtil.parseObj(sign).get("code")!=200){
                //发邮件提醒

            }
        }catch (Exception e){
            //发邮件
        }

    }
}
