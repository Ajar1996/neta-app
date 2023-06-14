package com.neta.app.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/6/12 10:12
 */
@Data
public class Token implements Serializable {
    String refreshToken;
    String authorization;
}
