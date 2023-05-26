package com.neta.app.emnu;

/**
 * @author: Ajar
 * @time: 2023/5/26 14:25
 */
public enum RequestEnum {

    sign("https://appapi-pki.chehezhi.cn:18443/hznz/customer/sign"),
    getArticleList("https://appapi-pki.chehezhi.cn:18443/hznz/app_article/common/article/rec/list"),
    insertArtComment("https://api.chehezhi.cn/hznz/app_article_comment/insertArtComment"),

    forwarArticle("https://appapi-pki.chehezhi.cn:18443/hznz/app_article/forwarArticle");



    private RequestEnum(String url) {
        this.url = url;
    }

    private String url;

    public String getUrl() {
        return this.url;
    }

}
