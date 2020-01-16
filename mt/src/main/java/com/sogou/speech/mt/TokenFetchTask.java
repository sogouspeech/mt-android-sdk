// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.mt;

import android.content.Context;

import com.google.protobuf.Duration;
import com.sogou.sogocommon.utils.CommonSharedPreference;
import com.sogou.sogocommon.utils.HttpsUtil;
import com.sogou.sogocommon.utils.LogUtil;
import com.sogou.speech.auth.v1.CreateTokenRequest;
import com.sogou.speech.auth.v1.CreateTokenResponse;
import com.sogou.speech.auth.v1.authGrpc;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.NegotiationType;
import io.grpc.okhttp.OkHttpChannelProvider;
import io.grpc.stub.StreamObserver;

public class TokenFetchTask {
    private static final String TAG = TokenFetchTask.class.getSimpleName();
    private Context context;
    private TokenFetchListener tokenFetchListener;
    /**
     * 8小时更换一次token
     */
    private static long TIME_EXP = 8 * 60 * 60;

    public TokenFetchTask(Context context, TokenFetchListener listener) {
        this.context = context;
        this.tokenFetchListener = listener;
    }

    public void execute() {
        try {
            grpcRequestToken();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "Exception:" + e.getMessage());
        }
    }

    private void onGainTokenSuccess(String token, long tokenExp) {
        CommonSharedPreference.getInstance(context).setString(OnlineMtConstants.TOKEN, token);
        CommonSharedPreference.getInstance(context).setLong(OnlineMtConstants.TOKEN_DURATION, tokenExp);
        tokenFetchListener.onTokenFetchSucc(token);
    }

    private void grpcRequestToken() throws Exception {
        LogUtil.d("TokenFetchTask", "grpc request token");
        /**
         * token间隔
         */
        Duration duration = Duration.newBuilder().setSeconds(TIME_EXP).build();
        /**
         * url
         * Appid
         * Appkey
         * Uuid
         */
        String url = CommonSharedPreference.getInstance(context).getString(OnlineMtConstants.URL, "");
        String appId = CommonSharedPreference.getInstance(context).getString(OnlineMtConstants.APPID, "");
        String appKey = CommonSharedPreference.getInstance(context).getString(OnlineMtConstants.APPKEY, "");
        String Uuid = CommonSharedPreference.getInstance(context).getString(OnlineMtConstants.UUID, "");

        CreateTokenRequest request = CreateTokenRequest.newBuilder()
                .setExp(duration)
                .setAppid(appId)
                .setAppkey(appKey)
                .setUuid(Uuid)
                .buildPartial();
        ManagedChannel channel = new OkHttpChannelProvider().builderForAddress(url, 443)
                .negotiationType(NegotiationType.TLS)
                .overrideAuthority(url + ":443")
                .sslSocketFactory(HttpsUtil.getSSLSocketFactory(null, null, null))
                .build();
        authGrpc.authStub client = authGrpc.newStub(channel);
        client.createToken(request, new StreamObserver<CreateTokenResponse>() {
            @Override
            public void onNext(CreateTokenResponse tokenResponse) {
                onGainTokenSuccess(tokenResponse.getToken(), tokenResponse.getEndTime().getSeconds());
                LogUtil.d("token:" + tokenResponse.getToken() + ",timestamp:" + tokenResponse.getEndTime().getSeconds());
                LogUtil.d("timestamp remain seconds: " +
                        (((tokenResponse.getEndTime().getSeconds() - 60 * 30) * 1000 - System.currentTimeMillis()) / 1000 / 60));
            }

            @Override
            public void onError(Throwable t) {
                tokenFetchListener.onTokenFetchFailed("error:" + t.getLocalizedMessage());
                LogUtil.e("onError " + t.getMessage());
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    public interface TokenFetchListener {
        void onTokenFetchSucc(String result);

        void onTokenFetchFailed(String errMsg);
    }
}