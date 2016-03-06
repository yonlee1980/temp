package com.jikexueyuan.yonshareposition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.baidu.android.pushservice.PushMessageReceiver;

import java.util.List;

public class MyPushReceiver extends PushMessageReceiver {
    SharedPreferences shared;

    @Override
    public void onBind(Context context, int i, String s, String s1, String s2, String s3) {
        shared = context.getSharedPreferences("share",Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);
    }

    @Override
    public void onUnbind(Context context, int i, String s) {

    }

    @Override
    public void onSetTags(Context context, int i, List<String> list, List<String> list1, String s) {

    }

    @Override
    public void onDelTags(Context context, int i, List<String> list, List<String> list1, String s) {

    }

    @Override
    public void onListTags(Context context, int i, List<String> list, String s) {

    }

    @Override
    public void onMessage(Context context, String s, String s1) {

    }

    @Override
    public void onNotificationClicked(Context context, String s, String s1, String s2) {

    }

    @Override
    public void onNotificationArrived(Context context, String s, String s1, String s2) {
        SharedPreferences.Editor editor = shared.edit();
        editor.putString("ip",s1);
        editor.commit();
    }
}
