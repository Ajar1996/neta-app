package com.neta.app.emnu;

/**
 * @author: Ajar
 * @time: 2023/5/26 14:25
 */
public enum commentsEnum {

    comments1("感谢楼主无私分享"),
    comments2("谢谢你，分享侠"),
    comments3("楼主发贴辛苦了，谢谢楼主的精彩分享"),
    comments4("每天一评论，积分我拿走"),
    comments5("楼主我来顶你了"),
    comments6("可以的，很帅"),
    comments7("必须的！"),
    comments8("牛的牛的"),
    comments9("加油加油！"),
    comments10("我觉得很不错");

    private commentsEnum(String url) {
        this.url = url;
    }

    private String url;

    public String getUrl() {
        return this.url;
    }

}
