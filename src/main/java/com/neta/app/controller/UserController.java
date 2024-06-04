package com.neta.app.controller;


import cn.hutool.core.util.RandomUtil;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
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

}
