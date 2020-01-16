package com.sogou.speech.mt;

import android.content.Context;
import android.text.TextUtils;

import com.sogou.sogocommon.utils.CommonSharedPreference;
import com.sogou.sogocommon.utils.LogUtil;

import org.conscrypt.Conscrypt;

import java.security.Security;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Date:2020/1/15
 * Author:zhangxiaobei
 * Describe:
 */
public class SogouTranslate {
    //语种列表
    public static final String CHINESE = "zh-cmn-Hans";
    public static final String ENGLISH = "en";
    public static final String JAPANESE = "ja";
    public static final String KOREAN = "ko";
    public static final String FRENCH = "fr";
    public static final String SPANISH = "es";
    public static final String RUSSIAN = "ru";
    public static final String GERMAN = "de";
    private static final String TAG = SogouTranslate.class.getSimpleName();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));
    private TranslateTask translateTask;
    private TranslateListener translateListener;
    private static Context context;

    static {
        if (Conscrypt.isAvailable()) {
            Security.insertProviderAt(Conscrypt.newProvider("GmsCore_OpenSSL"), 1);
        }
    }

    public SogouTranslate(TranslateListener translateListener) {
        if (translateListener == null) {
            throw new IllegalArgumentException("translateListener is null");
        }
        this.translateListener = translateListener;
    }

    public static boolean init(Context context, ZhiyinInitInfo info) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        if (info == null) {
            throw new IllegalArgumentException("info is null");
        }
        context = context.getApplicationContext();
        return initInfo(context, info);

    }

    public void translate(TranslateRequestConfig translateRequestConfig) {
        check(translateRequestConfig);
        translateTask = new TranslateTask(context, translateListener, translateRequestConfig);
        if (threadPoolExecutor != null) {
            threadPoolExecutor.submit(translateTask);
        }
    }

    private static boolean initInfo(Context context, ZhiyinInitInfo info) {
        if (TextUtils.isEmpty(info.baseUrl)) {
            throw new IllegalArgumentException("url is empty!");
        }
        CommonSharedPreference.getInstance(context).setString(OnlineMtConstants.URL, info.baseUrl);
        if (TextUtils.isEmpty(info.uuid)) {
            throw new IllegalArgumentException("uuid is empty!");
        }
        CommonSharedPreference.getInstance(context).setString(OnlineMtConstants.UUID, info.uuid);
        if (TextUtils.isEmpty(info.appid)) {
            throw new IllegalArgumentException("appid is empty!");
        }
        CommonSharedPreference.getInstance(context).setString(OnlineMtConstants.APPID, info.appid);
        if (TextUtils.isEmpty(info.appkey)) {
            throw new IllegalArgumentException("appkey is empty!");
        }
        CommonSharedPreference.getInstance(context).setString(OnlineMtConstants.APPKEY, info.appkey);
        try {
            /**
             * 获取token
             */
            AuthManager.getInstance().fetchTokenBlocked(context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "Exception:" + e.getMessage());
            return false;
        }

    }

    private void check(TranslateRequestConfig requestConfig) {
        if (requestConfig == null) {
            throw new IllegalArgumentException("requestConfig is null");
        }
        int contentLength = requestConfig.getContent().getBytes().length;
        if (contentLength > 2048) {
            throw new IllegalStateException("文本字节长度不能超过2048字节，当前长度：" + contentLength);
        }
        switch (requestConfig.getFromCode()) {
            case CHINESE:
                if (requestConfig.getDestCode() == CHINESE) {
                    throw new IllegalArgumentException("中文不能翻译成中文！");
                }
                break;
            case ENGLISH:
                if (requestConfig.getDestCode() != CHINESE) {
                    throw new IllegalArgumentException("英文只能翻译成中文！");
                }

                break;

            case JAPANESE:
                if (requestConfig.getDestCode() != CHINESE) {
                    throw new IllegalArgumentException("日文只能翻译成中文！");
                }

                break;

            case KOREAN:
                if (requestConfig.getDestCode() != CHINESE) {
                    throw new IllegalArgumentException("韩文只能翻译成中文！");
                }

                break;
            case FRENCH:
                if (requestConfig.getDestCode() != CHINESE) {
                    throw new IllegalArgumentException("法文只能翻译成中文！");
                }

                break;
            case SPANISH:
                if (requestConfig.getDestCode() != CHINESE) {
                    throw new IllegalArgumentException("西班牙文只能翻译成中文！");
                }

                break;
            case RUSSIAN:
                if (requestConfig.getDestCode() != CHINESE) {
                    throw new IllegalArgumentException("俄文只能翻译成中文！");
                }

                break;
            case GERMAN:
                if (requestConfig.getDestCode() != CHINESE) {
                    throw new IllegalArgumentException("德文只能翻译成中文！");
                }

                break;
        }
    }


    public void release() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdownNow();
        }
        LogUtil.d(TAG,"release");
    }


}


