package com.neta.app.service;

import com.neta.app.model.NetaResponse;
import com.neta.app.model.Token;

import java.util.List;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 17:28
 */
public interface RequestService {
    int forwarArticle(String groupId, String authorization);

    int insertArtComment(String openId, String groupId, String authorization);

    List<NetaResponse> getArticleList(String authorization);

    int sign(String authorization);

    Token refreshToken(String refreshToken);

}
