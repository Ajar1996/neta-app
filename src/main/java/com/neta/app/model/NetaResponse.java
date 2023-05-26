package com.neta.app.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 17:34
 */
@Data
@Builder
public class NetaResponse implements Serializable {

    String openId;
    String groupId;
}
