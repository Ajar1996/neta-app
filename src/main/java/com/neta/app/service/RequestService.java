package com.neta.app.service;

import com.neta.app.model.NetaResponse;

import java.util.List;

/**
 * @description:
 * @author: Ajar
 * @time: 2023/5/26 17:28
 */
public interface RequestService {
    int forwarArticle(String groupId);

    int insertArtComment(String openId, String groupId);

    List<NetaResponse> getArticleList();
}
