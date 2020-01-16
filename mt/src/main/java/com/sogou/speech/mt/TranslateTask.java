package com.sogou.speech.mt;

import android.content.Context;

/**
 * Date:2020/1/15
 * Author:zhangxiaobei
 * Describe:
 */
public class TranslateTask implements Runnable {

    TranslateProtocol translateProtocol;
    TranslateRequestConfig translateRequestConfig;

    public TranslateTask(Context context, TranslateListener translateListener, TranslateRequestConfig translateRequestConfig) {
        translateProtocol = new TranslateProtocol(context, translateListener);
        this.translateRequestConfig = translateRequestConfig;
    }


    @Override
    public void run() {
        if (translateProtocol != null) {
            translateProtocol.translate(translateRequestConfig);
        }

    }
}
