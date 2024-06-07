package com.neta.app.service;

import com.neta.app.entity.User;
import com.neta.app.model.NetaResponse;
import com.neta.app.model.Token;

import java.util.List;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 17:28
 */
public interface RequestService {
    int forwarArticle(String groupId, String authorization) throws Exception;

    int insertArtComment(String openId, String groupId, String authorization) throws Exception;

    List<NetaResponse> getArticleList(String authorization) throws Exception;

    int sign(User user) throws Exception;

    Token refreshToken(String refreshToken) throws Exception;

    void checkSign(User user) throws Exception;

    void getLuckyNum(User user,String turntableId) ;

    String userLogin(User user);
    Integer selectTurntableList(String turntableId);

     void getCustomer(User user);

}
