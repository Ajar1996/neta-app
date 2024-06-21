package com.neta.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author springBoot-Learning
 * @since 2024-06-21
 */
public class Ip implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ip;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String city;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Ip{" +
            "ip=" + ip +
            ", id=" + id +
            ", city=" + city +
        "}";
    }
}
