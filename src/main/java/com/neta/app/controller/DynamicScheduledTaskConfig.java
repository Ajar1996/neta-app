package com.neta.app.controller;

import cn.hutool.core.util.RandomUtil;
import com.neta.app.entity.User;
import com.neta.app.model.NetaResponse;
import com.neta.app.model.Token;
import com.neta.app.model.TokenConfiguration;
import com.neta.app.service.RequestService;
import com.neta.app.service.SchedulingTaskBasic;
import com.neta.app.service.impl.MailService;
import com.neta.app.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@Slf4j
public class DynamicScheduledTaskConfig implements SchedulingConfigurer {

    @Resource
    RequestService requestService;

    @Resource
    TokenConfiguration tokenConfiguration;

    @Resource
    UserServiceImpl userService;

    @Resource
    MailService mailService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(1));
        taskRegistrar.addTriggerTask(
                () -> sign(),
                triggerContext -> {
                    String cron = generateCronExpression(); // Dynamically generate cron expression
                    return new CronTrigger(cron).nextExecutionTime(triggerContext);
                }
        );
        taskRegistrar.addTriggerTask(
                () -> checkSign(),
                triggerContext -> {
                    String cron =  "0 0 20 * * ?"; // Dynamically generate cron expression
                    return new CronTrigger(cron).nextExecutionTime(triggerContext);
                }
        );
    }

    public void checkSign() {
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



    public void sign() {
        List<User> userList = userService.list();
        Collections.shuffle(userList);
        for (User user : userList) {
            try {
                log.info("{}开始执行，id为{}", user.getName(), user.getId());
                Thread.sleep(RandomUtil.randomInt(12000, 20000));
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
                    Thread.sleep(RandomUtil.randomInt(10000, 15000));
                    requestService.forwarArticle(netaResponse.getGroupId(), authorization);
                }
                requestService.sign(authorization);
                log.info("{}执行成功,id为{}", user.getName(), user.getId());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}执行失败", user.getName());
                mailService.sendSimpleMail(user.getEmail(), "哪吒APP签到失败", "请登录网站刷新你的授权码");
            }
        }
    }

    public String generateCronExpression() {
        int i=new java.util.Random().nextInt(9);
        i=i+6;
        String time="0 0 "+i+" * * ?";
        log.info("下次签到时间为{}点",i);
        return time;
        // Generate cron expression dynamically here. For example, every minute:
    }

}