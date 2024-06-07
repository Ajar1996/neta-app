package com.neta.app.controller;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.neta.app.emnu.RequestEnum;
import com.neta.app.entity.User;
import com.neta.app.model.NetaResponse;
import com.neta.app.model.Token;
import com.neta.app.model.TokenConfiguration;
import com.neta.app.service.RequestService;
import com.neta.app.service.impl.MailService;
import com.neta.app.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author springBoot-Learning
 * @since 2023-07-21
 */
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {


    @Resource
    RequestService requestService;


    @Resource
    UserServiceImpl userService;

    @Resource
    DynamicScheduledTaskConfig dynamicScheduledTaskConfig;

    @PostMapping("/insert")
    public void insert(@RequestBody User user) throws Exception {
        log.info("{}开始签到", user.getName());
        Random random = new Random();
        // 生成4个0-255之间的随机数
        int octet1 = random.nextInt(256);
        int octet2 = random.nextInt(256);
        int octet3 = random.nextInt(256);
        int octet4 = random.nextInt(256);

        // 格式化成IP地址字符串
        String ipAddress = String.format("%d.%d.%d.%d", octet1, octet2, octet3, octet4);

        user.setIp(ipAddress);

        Token token = requestService.refreshToken(user.getRefreshToken());

        user.setRefreshToken(token.getRefreshToken());
        user.setAuthorization(token.getAuthorization());
        userService.save(user);

        requestService.sign(user);
        log.info("{}执行结束", user.getName());
    }

    @GetMapping ("/sign")
    public void sign()  {
        log.info("开始签到");
        dynamicScheduledTaskConfig.sign();
        log.info("执行结束");
    }
    @GetMapping ("/test")
    public void getLuckyNum() throws Exception {
        List<User> userList = userService.list();
        for (User user : userList) {
            Token refreshToken = requestService.refreshToken(user.getRefreshToken());
            if (refreshToken == null) {
                continue;
            }
            user.setRefreshToken(refreshToken.getRefreshToken());
            user.setAuthorization(refreshToken.getAuthorization());
            requestService.getCustomer(user);
            userService.updateById(user);
        }
    }

    @GetMapping ("/getLuckyNum/{id}")
    public void getLuckyNum(@PathVariable("id") String id) throws Exception {
        log.info("开始抽奖");
        Integer status=requestService.selectTurntableList(id);

        if (status==1){
            List<User> userList = userService.list();
            for (User user : userList) {
                try {
                    Integer status2=requestService.selectTurntableList(id);
                    if (status2==1){
                    log.info("{}开始执行，id为{}", user.getName(), user.getId());
                    Token refreshToken = requestService.refreshToken(user.getRefreshToken());
                    if (refreshToken == null|| StrUtil.isBlank(user.getWechatId())) {
                        continue;
                    }
                    String token =requestService.userLogin(user);
                    user.setRefreshToken(refreshToken.getRefreshToken());
                    user.setAuthorization(refreshToken.getAuthorization());
                    user.setToken(token);

                    //更新token
                    userService.updateById(user);
                    requestService.getLuckyNum(user,id);

                    log.info("{}执行成功,id为{}", user.getName(), user.getId());
                    }else {
                        log.info("抽完咯，没有了");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("{}执行失败", user.getName());
                }
            }
        }

        log.info("抽奖执行结束");
    }

}
