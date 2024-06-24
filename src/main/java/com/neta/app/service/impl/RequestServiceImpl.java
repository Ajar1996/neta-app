package com.neta.app.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neta.app.emnu.RequestEnum;
import com.neta.app.emnu.commentsEnum;
import com.neta.app.entity.Event;
import com.neta.app.entity.Ip;
import com.neta.app.entity.User;
import com.neta.app.model.NetaResponse;
import com.neta.app.model.Token;
import com.neta.app.service.RequestService;
import freemarker.template.utility.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 17:31
 */
@Service
@Slf4j
public class RequestServiceImpl implements RequestService {

    @Resource
    UserServiceImpl userService;

    @Resource
    EventServiceImpl eventService;

    @Resource
    IpServiceImpl ipService;
    @Override
    public int forwarArticle(String openId, String authorization) throws Exception {
        //转发帖子
        String forwar =
                HttpRequest.put(RequestEnum.forwarArticle.getUrl())
                        .header(Header.CONTENT_TYPE, "application/json;charset=utf-8")
                        .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                        .body("{\"articleId\":\"" + openId + "\",\"forwardTo\":\"1\"}")//表单内容
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
    public int sign(User user) throws Exception {
        //签到
        String sign = HttpRequest.get(RequestEnum.sign.getUrl())
                .header(Header.AUTHORIZATION, user.getAuthorization())//头信息，多个头信息多次调用此方法即可
                .header("X-Forwarded-For", user.getIp())
                .header("Client-IP", user.getIp())
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
        log.info(sign);
        eventService.save(Event.builder().event("签到").userId(user.getId()).message(sign).build());
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
    public void checkSign(User user) throws Exception {
        log.info("{}开始检查是否签到", user.getId());
        Thread.sleep(RandomUtil.randomInt(12000, 20000));
        Token token = this.refreshToken(user.getRefreshToken());

        user.setRefreshToken(token.getRefreshToken());
        user.setAuthorization(token.getAuthorization());
        //更新token
        userService.updateById(user);

        //检查是否签到
        String checkSignResponse = HttpRequest.get(RequestEnum.checkSign.getUrl())
                .header(Header.AUTHORIZATION, user.getAuthorization())//头信息，多个头信息多次调用此方法即可
                .timeout(40000)//超时，毫秒
                .execute().body();
        Integer checkSign = ((Integer) JSONUtil.parseObj(JSONUtil.parseObj(checkSignResponse).get("data")).get("sign"));
        if (checkSign != null && checkSign == 0) {
            this.sign(user);
            log.info("{}补签成功", user.getId());
        }
    }

    @Override
    public void getLuckyNum(User user,String turntableId)  {

        //幸运抽奖
        String getLuckyNum = HttpRequest.post(RequestEnum.getLuckyNum.getUrl())
                .header(Header.AUTHORIZATION, user.getAuthorization())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Forwarded-For", user.getIp())
                .header("Client-IP", user.getIp())
                .form("token", user.getToken())
                .form("turntableId", turntableId)
                .timeout(40000)//超时，毫秒
                .execute().body();
        log.info("幸运抽奖: {}", getLuckyNum);
        eventService.save(Event.builder().event("幸运抽奖").userId(user.getId()).message(getLuckyNum).build());

    }

    @Override
    public void getCustomer(User user)  {
        //幸运抽奖
        String getCustomer = HttpRequest.get(RequestEnum.getCustomer.getUrl())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Forwarded-For", user.getIp())
                .header(Header.AUTHORIZATION, user.getAuthorization())
                .timeout(40000)//超时，毫秒
                .execute().body();

        String userUuid = ((String) JSONUtil.parseObj(JSONUtil.parseObj(getCustomer).get("data")).get("userUuid"));
        if (StrUtil.isNotBlank(userUuid)){
            user.setUserUuid(userUuid);
        }

        String wechatId = ((String) JSONUtil.parseObj(JSONUtil.parseObj(getCustomer).get("data")).get("wechatId"));
        if (StrUtil.isNotBlank(wechatId)){
            user.setWechatId(wechatId);
        }
    }

    @Override
    public String getCityIp(User user)  {
        String getCustomer = HttpRequest.get(RequestEnum.getCustomer.getUrl())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Forwarded-For", user.getIp())
                .header(Header.AUTHORIZATION, user.getAuthorization())
                .timeout(40000)//超时，毫秒
                .execute().body();

        String address = ((String) JSONUtil.parseObj(JSONUtil.parseObj(getCustomer).get("data")).get("address"));
        if (StrUtil.isNotBlank(address)){
            String [] split=address.split("-");
            log.info("城市为{}",split[1]);
           List<Ip> ipList= ipService.getBaseMapper().selectList(new LambdaQueryWrapper<Ip>().eq(Ip::getCity,split[1]));
           if (ipList.isEmpty()){
               return this.getIp();
           }
            Random random = new Random();
            String ip=ipList.get(random.nextInt(ipList.size())).getIp();
            log.info("ip为{}",ip);
            return ip;
        }
        return this.getIp();
    }


    /*
     * 随机生成国内IP地址
     */
    @Override
    public String getIp(){

        //ip范围
        int[][] range = {{607649792,608174079},//36.56.0.0-36.63.255.255
                {1038614528,1039007743},//61.232.0.0-61.237.255.255
                {1783627776,1784676351},//106.80.0.0-106.95.255.255
                {2035023872,2035154943},//121.76.0.0-121.77.255.255
                {2078801920,2079064063},//123.232.0.0-123.235.255.255
                {-1950089216,-1948778497},//139.196.0.0-139.215.255.255
                {-1425539072,-1425014785},//171.8.0.0-171.15.255.255
                {-1236271104,-1235419137},//182.80.0.0-182.92.255.255
                {-770113536,-768606209},//210.25.0.0-210.47.255.255
                {-569376768,-564133889}, //222.16.0.0-222.95.255.255
        };

        Random rdint = new Random();
        int index = rdint.nextInt(10);
        String ip = num2ip(range[index][0]+new Random().nextInt(range[index][1]-range[index][0]));
        return ip;
    }

    /*
     * 将十进制转换成ip地址
     */
    public static String num2ip(int ip) {
        int [] b=new int[4] ;
        String x = "";

        b[0] = (int)((ip >> 24) & 0xff);
        b[1] = (int)((ip >> 16) & 0xff);
        b[2] = (int)((ip >> 8) & 0xff);
        b[3] = (int)(ip & 0xff);
        x=Integer.toString(b[0])+"."+Integer.toString(b[1])+"."+Integer.toString(b[2])+"."+Integer.toString(b[3]);

        return x;
    }

    @Override
    public String userLogin(User user)  {
        String userLogin = HttpRequest.post(RequestEnum.userLogin.getUrl())
                .header(Header.AUTHORIZATION, user.getAuthorization())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Forwarded-For", user.getIp())
                .header("Client-IP", user.getIp())
                .form("phone", user.getPhone())
                .form("access_token", user.getAuthorization())
                .form("refresh_token", user.getRefreshToken())
                .form("type", "2")
                .form("uuid", user.getUserUuid())
                .form("openId", user.getWechatId())
                .form("inviter_uuid", "")
                .form("activity_id", "0")
                .form("token", generateToken())
                .timeout(40000)
                .execute()
                .body();

        // 可以根据需要处理响应
        log.info("微信登录响应: {}", userLogin);

        String token= (String) JSONUtil.parseObj(
                JSONUtil.parseObj(
                        JSONUtil.parseObj(
                                JSONUtil.parseObj(userLogin).get("data")).get("data")).get("info")).get("token");

        return token;
    }

    public static String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    @Override
    public Integer selectTurntableList(String turntableId)  {
        //幸运抽奖
        String selectTurntableList = HttpRequest.get(RequestEnum.selectTurntableList.getUrl())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Forwarded-For", "192.168.1.1")
                .form("token", generateToken())
                .form("turntableId", turntableId)
                .timeout(40000)//超时，毫秒
                .execute().body();
        log.info("获取抽奖列表 {}", selectTurntableList);
        JSONObject turntableJson = JSONUtil.parseObj(selectTurntableList);
        JSONArray jsonArray = (JSONArray) turntableJson.get("data");
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);
        Integer status = (Integer) jsonObject.get("status");
        return status;
    }


}
