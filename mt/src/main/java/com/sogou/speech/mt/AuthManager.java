// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.sogou.speech.mt;

import android.content.Context;
import android.text.TextUtils;

import com.sogou.sogocommon.utils.CommonSharedPreference;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class AuthManager {
    private static AuthManager sAuthManager;

    private boolean mOnlineStatus = false;
    private boolean mOfflineStatus = false;
    private int mErrorOfflineStatus = 0;

    private boolean mFetchTokenSuccess = false;

    private String mErrMsg = null;

    private final CyclicBarrier sCyclicBarrier = new CyclicBarrier(2);

    public static AuthManager getInstance() {
        if (sAuthManager == null) {
            synchronized (AuthManager.class) {
                if (sAuthManager == null) {
                    sAuthManager = new AuthManager();
                }
            }
        }
        return sAuthManager;
    }

    public int getErrorStatus() {
        return mErrorOfflineStatus;
    }

    public boolean isAuthPassed(int mode) {
        return mOnlineStatus;

    }

    private AuthManager() {
    }

    /**
     * 获取token，串行操作等待结果后再返回
     *
     * @return
     */
    public void fetchTokenBlocked(final Context context) throws Exception {
        /**
         * 如果不需要刷新token直接返回
         */
        if (!needUpdateToken(context)) {
            return;
        }
        String url = CommonSharedPreference.getInstance(context).getString(OnlineMtConstants.URL, "");
        if (TextUtils.isEmpty(url)) {
            throw new IllegalAccessException("url is empty!");
        }
        new TokenFetchTask(context, new TokenFetchTask.TokenFetchListener() {
            @Override
            public void onTokenFetchSucc(String result) {
                if (TextUtils.isEmpty(result)) {
                    mFetchTokenSuccess = false;
                    mErrMsg = "token is null!";
                } else {
                    CommonSharedPreference.getInstance(context).setString(OnlineMtConstants.TOKEN, result);
                    mFetchTokenSuccess = true;
                }
                try {
                    sCyclicBarrier.await();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTokenFetchFailed(String errMsg) {
                mFetchTokenSuccess = false;
                mErrMsg = errMsg;
                try {
                    sCyclicBarrier.await();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).execute();
        try {
            sCyclicBarrier.await();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!mFetchTokenSuccess) {
            throw new IllegalStateException(mErrMsg);
        }
    }

    private boolean needUpdateToken(Context context) {
        long duration = CommonSharedPreference.getInstance(context).getLong(OnlineMtConstants.TOKEN_DURATION, (long) 0);
        if (duration == 0) {
            return true;
        }
        /**
         * 如果当前token在半小时之内过期，则刷新token
         */
        long timeGap = (duration - 60 * 30) * 1000;
        return (timeGap - System.currentTimeMillis() < 0);
    }
}