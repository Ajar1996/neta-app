package com.neta.app.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.neta.app.emnu.RequestEnum;
import com.neta.app.emnu.commentsEnum;
import com.neta.app.entity.User;
import com.neta.app.model.NetaResponse;
import com.neta.app.model.Token;
import com.neta.app.model.TokenConfiguration;
import com.neta.app.service.impl.MailService;
import com.neta.app.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
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

    @Resource
    UserServiceImpl userService;

    @Resource
    MailService mailService;

    static int i=3;

    String I="0 0 1 * * ?";

   // @Scheduled(cron = "0 0 0 * * ?")
    public String getSignTime() throws InterruptedException {
        int i=new java.util.Random().nextInt(9);
        i=i+6;
        String time="0 0 "+i+" * * ?";
        log.info("下次签到时间为{}点",i);
        return time;
    }

    /**
     * 每天随机时间执行
     */
    //@Scheduled(cron = "#{@this.getSignTime()}")
    //@Scheduled(fixedDelayString = "#{ T(java.util.concurrent.TimeUnit).HOURS.toMillis(new java.util.Random().nextInt(17)) }")
    //@Scheduled(fixedDelayString = "#{ T(java.util.concurrent.TimeUnit).HOURS.toMillis(this.i) }")
    public void sign() throws InterruptedException {

        List<User> userList = userService.list();
        Collections.shuffle(userList);
        for (User user : userList) {
            try {
                log.info("{}开始执行，id为{}", user.getName(),user.getId());
                Token token = requestService.refreshToken(user.getRefreshToken());
                if (token == null) {
                    continue;
                }
                user.setRefreshToken(token.getRefreshToken());
                    user.setAuthorization(token.getAuthorization());
                //更新token
                userService.updateById(user);


                String authorization = user.getAuthorization();
                List<NetaResponse> netaResponses = requestService.getArticleList(authorization);
                for (NetaResponse netaResponse : netaResponses) {
                    //休眠，避免被发现是脚本
                    Thread.sleep(RandomUtil.randomInt(2000, 3000));
                    requestService.forwarArticle(netaResponse.getOpenId(), authorization);
                }
                requestService.sign(user);
                log.info("{}执行成功,id为{}", user.getName(),user.getId());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}执行失败", user.getName());
                mailService.sendSimpleMail(user.getEmail(), "哪吒APP签到失败", "请登录网站刷新你的授权码");
            }

        }
    }

   // @Scheduled(cron = "0 0 20 * * ?")
    private void checkSign() throws InterruptedException {
        List<User> userList = userService.list();
        for (User user : userList) {
            try {
                requestService.checkSign(user);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}还没有签到,id为{}", user.getName(),user.getId());
             //   mailService.sendSimpleMail(user.getEmail(), "哪吒APP签到提醒", "你今天的app还没有签到，请检查");
            }
        }
    }


    //@Scheduled(fixedDelayString = "#{ T(java.util.concurrent.TimeUnit).HOURS.toMillis(new java.util.Random().nextInt(17)) }")
    private void checkSign1() throws InterruptedException {
        List<User> userList = userService.list();
        Collections.shuffle(userList);
        commentsEnum.valueOf("comments4");
        for (User user : userList) {
            try {
                Thread.sleep(3000);
                Token token = requestService.refreshToken(user.getRefreshToken());
                String getCustomer = HttpRequest.post("https://api.chehezhi.cn/hznz/app_article/insertArtComment")
                        .header(Header.AUTHORIZATION, token.getAuthorization())//头信息，多个头信息多次调用此方法即可
                        .header("X-Forwarded-For", user.getIp())
                        .header("Client-IP", user.getIp())
                        .body("{\"content\":\"" +
                                commentsEnum.valueOf("comments" + RandomUtil.randomInt(1, 10)).getUrl()+
                                "\",\"articleId\":\"" +
                                376780 +
                                "\"}")//表单内容
                        .timeout(40000)//超时，毫秒
                        .execute().body();
                log.info(getCustomer);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}还没有签到,id为{}", user.getName(),user.getId());
                //   mailService.sendSimpleMail(user.getEmail(), "哪吒APP签到提醒", "你今天的app还没有签到，请检查");
            }
        }
    }

}
