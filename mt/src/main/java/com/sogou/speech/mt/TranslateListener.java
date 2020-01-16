package com.sogou.speech.mt;


import com.sogou.speech.mt.v1.TranslateTextResponse;

public interface TranslateListener {
    void onNext(TranslateTextResponse value);

    void onError(Throwable t);

    void onCompleted();
}
