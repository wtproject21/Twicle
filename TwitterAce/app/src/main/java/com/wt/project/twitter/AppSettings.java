package com.wt.project.twitter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AppSettings {
    static boolean widget_auto=true;
    static boolean alarmSet=false;
    static boolean comand=false;
    static private Intent widgetUpdateIntent=null;
    static private Context widgetContext=null;
    static private PendingIntent updateWidget=null;
    static private String PREF_NAME="Settings";
    static void setWidget_auto(Boolean b){
        if(b&&!widget_auto&&!alarmSet){
            if(widgetUpdateIntent!=null){
                widgetContext.sendBroadcast(widgetUpdateIntent);
            }
        }
        widget_auto=b;
    }
    static void storeWidget_auto(Context context,boolean b){
        //トークンの設定
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("widget_auto",b);
        //トークンの保存
        editor.commit();
    }
    static boolean loadWidget_auto(Context context,boolean default_boolean){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean("widget_auto",default_boolean);
    }
    static void setWidgetIntent(Context widgetContext,Intent widgetUpdateIntent,Boolean override){
        if(override||AppSettings.widgetContext==null){
            AppSettings.widgetUpdateIntent=widgetUpdateIntent;
            AppSettings.widgetContext=widgetContext;
        }
    }
}
