package com.neta.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 13:42
 */
@SpringBootApplication
@EnableScheduling
public class NetaApplication {
    public static void main(String[] args) {
        //每天自动签到，评论，转发 获得5+2*3+1*3=14积分

        SpringApplication.run(NetaApplication.class, args);

    }
}
