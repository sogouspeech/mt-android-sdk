package com.sogou.speech.mt;

import android.content.Context;

import com.sogou.sogocommon.utils.CommonSharedPreference;
import com.sogou.sogocommon.utils.HttpsUtil;
import com.sogou.sogocommon.utils.LogUtil;
import com.sogou.speech.mt.v1.TranslateConfig;
import com.sogou.speech.mt.v1.TranslateTextRequest;
import com.sogou.speech.mt.v1.TranslateTextResponse;
import com.sogou.speech.mt.v1.mtGrpc;

import java.util.HashMap;

import javax.net.ssl.SSLSocketFactory;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.NegotiationType;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.okhttp.OkHttpChannelProvider;
import io.grpc.stub.StreamObserver;


public class TranslateProtocol {
    private static final String TAG = TranslateProtocol.class.getSimpleName();
    private mtGrpc.mtStub mtClient;
    private Context mContext;
    private ManagedChannel channel;
    private StreamObserver<TranslateTextResponse> mTranslateResponse = null;
    private TranslateListener translateListener;

    public TranslateProtocol(Context context, TranslateListener translateListener) {
        mContext = context;
        this.translateListener = translateListener;
        createMtClient();
    }

    private void createMtClient() {
        HashMap<String, String> headerParams = new HashMap<>();
        headerParams.put("Authorization", "Bearer " + CommonSharedPreference.getInstance(mContext).getString(OnlineMtConstants.TOKEN, ""));
        headerParams.put("appid", CommonSharedPreference.getInstance(mContext).getString(OnlineMtConstants.APPID, ""));
        headerParams.put("uuid", CommonSharedPreference.getInstance(mContext).getString(OnlineMtConstants.UUID, ""));

        String url = CommonSharedPreference.getInstance(mContext).getString(OnlineMtConstants.URL, "");

        SSLSocketFactory sslSocketFactory = null;
        try {
            sslSocketFactory = HttpsUtil.getSSLSocketFactory(null, null, null);
        } catch (Throwable t) {
            t.printStackTrace();
            LogUtil.e(TAG, "Throwable:" + t.getMessage());
        }

        OkHttpChannelBuilder okHttpChannelProvider = new OkHttpChannelProvider()
                .builderForAddress(url,
                        443)
                .overrideAuthority(url
                        + ":443")
                .negotiationType(NegotiationType.TLS)
                .intercept(new HeaderClientInterceptor(headerParams));

        if (sslSocketFactory != null) {
            okHttpChannelProvider.sslSocketFactory(sslSocketFactory);
        }

        channel = okHttpChannelProvider.build();
        mtClient = mtGrpc.newStub(channel);
    }

    public void translate(TranslateRequestConfig translateRequestConfig) {
        String content = translateRequestConfig.getContent();
        String fromCode = translateRequestConfig.getFromCode();
        String destCode = translateRequestConfig.getDestCode();
        mTranslateResponse = new StreamObserver<TranslateTextResponse>() {
            @Override
            public void onNext(TranslateTextResponse value) {
                if (value == null) {
                    LogUtil.e(TAG, "translation response is null");
                    return;
                }
                LogUtil.d(TAG, "TranslateTextResponse " + value.toString() + " getSourceText " + value.getSourceText() + "getTranslatedText " + value.getTranslatedText());
                if (translateListener != null) {
                    translateListener.onNext(value);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (t == null) {
                    return;
                }
                t.printStackTrace();
                LogUtil.e(TAG, "onError " + t.getMessage());
                if (translateListener != null) {
                    translateListener.onError(t);
                }
                release();
            }

            @Override
            public void onCompleted() {
                LogUtil.d(TAG, "translate onCompleted：" + this.hashCode());
                if (translateListener != null) {
                    translateListener.onCompleted();
                }
                release();

            }
        };


        TranslateTextRequest mTranslateRequest = TranslateTextRequest.newBuilder()
                .setConfig(TranslateConfig.newBuilder()
                        .setSourceLanguageCode(fromCode)
                        .setTargetLanguageCode(destCode)
                        .build())
                .setText(content)
                .build();
        mtClient.translateText(mTranslateRequest, mTranslateResponse);

    }

    private void release() {
        translateListener = null;
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
        LogUtil.d(TAG,"release");
    }
}
