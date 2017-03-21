package com.tianyou.sdk.demo;

import android.app.Application;

import com.tianyou.sdk.interfaces.TianyouSdk;

/**
 * Created by itstrong on 2017/3/21.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        String gameId = "11000";
        String gameToken = "33d6548e48d4318ceb0e3916a79afc84";
        String gameName = "测试";
        /**
         * gameId：app唯一标识，非常重要，请认真填写，确保正确
         * gameToken：appkey
         * gameName: 游戏名
         * isLandscape：游戏横屏为true，竖屏为false
         */
        TianyouSdk.getInstance().applicationInit(this, gameId, gameToken, gameName, true);
    }
}
