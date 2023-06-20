package com.neta.app.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/6/20 9:40
 */
@Configuration
@ConfigurationProperties(prefix = "user")
@Data
public class TokenConfiguration {
    HashMap<String, String> refreshToken;
}
