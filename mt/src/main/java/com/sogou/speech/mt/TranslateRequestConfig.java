package com.sogou.speech.mt;

/**
 * Date:2020/1/15
 * Author:zhangxiaobei
 * Describe:
 */
public class TranslateRequestConfig {
    private String content;
    private String fromCode;
    private String destCode;

    public TranslateRequestConfig() {

    }

    public TranslateRequestConfig(String content, String fromCode, String destCode) {
        this.content = content;
        this.fromCode = fromCode;
        this.destCode = destCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFromCode() {
        return fromCode;
    }

    public void setFromCode(String fromCode) {
        this.fromCode = fromCode;
    }

    public String getDestCode() {
        return destCode;
    }

    public void setDestCode(String destCode) {
        this.destCode = destCode;
    }


}
