package com.neta.app.controller;

import cn.hutool.core.util.RandomUtil;
import com.neta.app.entity.User;
import com.neta.app.model.Token;
import com.neta.app.model.TokenConfiguration;
import com.neta.app.service.RequestService;
import com.neta.app.service.impl.MailService;
import com.neta.app.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@Slf4j
public class SignTask implements SchedulingConfigurer {
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
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.initialize();
        taskRegistrar.setScheduler(scheduler);

        List<ScheduledFuture<?>> futures = new ArrayList<>();

        Runnable reconfigureTask = () -> {
            // Cancel previous tasks
            for (ScheduledFuture<?> future : futures) {
                future.cancel(false);
            }
            futures.clear();

            // Generate new tasks
            List<User> userList = userService.list();
            List<List<User>> userGroups = divideUsersIntoGroups(userList, 50); // Divide users into 10 groups

            for (int i = 0; i < userGroups.size(); i++) {
                log.info("组别i{}开始签到,人员为{}",i,userGroups.get(i).toString());
                int finalI = i;
                ScheduledFuture<?> future = scheduler.schedule(() -> sign(userGroups.get(finalI)),
                        triggerContext -> {
                            String cron = generateCronExpression(); // Dynamically generate cron expression
                            return new CronTrigger(cron).nextExecutionTime(triggerContext);
                        });
                futures.add(future);
            }
        };

        // Schedule reconfiguration task every 24 hours
        scheduler.scheduleWithFixedDelay(reconfigureTask, TimeUnit.HOURS.toMillis(24));
        // Initial task configuration
        reconfigureTask.run();
    }

    public void sign(List<User> userList) {
        log.info("开始签到");
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

                requestService.sign(user);
                log.info("{}执行成功,id为{}", user.getName(), user.getId());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}执行失败", user.getName());
                mailService.sendSimpleMail(user.getEmail(), "哪吒APP签到失败", "请登录网站刷新你的授权码");
            }
        }
        log.info("签到结束");
    }

    public String generateCronExpression() {
        //指定小时
        int hour = new java.util.Random().nextInt(9) + 6;
        //指定分钟
        int min = new java.util.Random().nextInt(55);

        String time = "0 " + min + " " + hour + " * * ?";
        log.info("下次签到时间为{}点{}分", hour, min);
        return time;
    }

    // Method to divide users into groups
    private List<List<User>> divideUsersIntoGroups(List<User> users, int groupCount) {
        List<List<User>> groups = new ArrayList<>();
        int groupSize = users.size() / groupCount;
        int remainder = users.size() % groupCount;

        Collections.shuffle(users);

        int startIndex = 0;
        for (int i = 0; i < groupCount; i++) {
            int endIndex = startIndex + groupSize + (remainder-- > 0 ? 1 : 0);
            groups.add(new ArrayList<>(users.subList(startIndex, endIndex)));
            startIndex = endIndex;
        }

        return groups;
    }
}
