package com.neta.app.emnu;

/**
 * @author: Ajar
 * @time: 2023/5/26 14:25
 */
public enum RequestEnum {

    sign("https://appapi-pki.chehezhi.cn:18443/hznz/customer/sign"),
    getArticleList("https://appapi-pki.chehezhi.cn:18443/hznz/app_article/common/article/rec/list"),
    insertArtComment("https://api.chehezhi.cn/hznz/app_article_comment/insertArtComment"),
    refreshToken("https://appapi-pki.chehezhi.cn:18443/customer/account/info/refreshApiToken"),
    forwarArticle("https://appapi-pki.chehezhi.cn:18443/hznz/app_article/forwarArticle"),

    getLuckyNum("https://api.chehezhi.cn/hznz/app_lucky_star/getLuckyNum"),

    userLogin("https://www.hozonauto.com/api_lottery/user_login"),
    getCustomer("https://appapi-pki.chehezhi.cn:18443/hznz/customer/getCustomer"),
    selectTurntableList("https://api.chehezhi.cn/hznz/app_lucky_star/selectTurntableList"),



    checkSign("https://appapi-pki.chehezhi.cn:18443/hznz/customer/getCustomer");


    private RequestEnum(String url) {
        this.url = url;
    }

    private String url;

    public String getUrl() {
        return this.url;
    }

}
