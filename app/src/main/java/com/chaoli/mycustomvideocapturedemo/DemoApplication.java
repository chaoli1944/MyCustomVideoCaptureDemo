package com.chaoli.mycustomvideocapturedemo;

import android.app.Application;

import com.chaoli.mycustomvideocapturedemo.customView.AppCore;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.rtmp.TXLiveBase;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCore.getInstance().init(this);

        //集成bugly
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(TXLiveBase.getSDKVersionStr());
        CrashReport.initCrashReport(getApplicationContext(), "6e3737e3a0", true, strategy);


    }
}
