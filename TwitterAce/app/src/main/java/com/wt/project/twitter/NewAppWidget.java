package com.wt.project.twitter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Timer;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {
    final int INTERVAL_PERIOD = 120;
    Timer timer = new Timer();
    AppWidgetManager appWidgetManager;
    int[] appWidgetIds;
    boolean enableUpdate=true;

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final int[] colors={Color.argb(200,100,150,100),Color.argb(200,150,100,100),Color.argb(200,100,100,150),Color.argb(0,0,0,0)};
        MyTweet mt;
        mt=new MyTweet(context);
        final int tweetShow=4;
        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        final int[] TVids={R.id.textView10,R.id.textView9,R.id.textView8,R.id.textView7};
        final int[] IVids={R.id.imageView,R.id.imageView2,R.id.imageView3,R.id.imageView4};
        final int[] TweetIds={R.id.widget_tweet1,R.id.widget_tweet2,R.id.widget_tweet3,R.id.widget_tweet4};
        final int refID=R.id.widget_ref_btn;
        final int modeID=R.id.widget_auto_btn;
        //views.setTextViewText(R.id.appwidget_text, widgetText);
        if(TwitterUtils.hasAccessToken(context)){
            //views.setTextViewText(R.id.textView10,"取得できます");
            final AppWidgetManager awm=appWidgetManager;
            final int awId=appWidgetId;

            Intent intent3=new Intent(context,NewAppWidget.class);
            intent3.setAction("refresh");
            intent3.putExtra("ID",appWidgetId);
            PendingIntent clickButton=PendingIntent.getBroadcast(context,0,intent3,PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(refID,clickButton);
            Intent intent4=new Intent(context,NewAppWidget.class);
            intent4.setAction("mode_change");
            intent4.putExtra("ID",appWidgetId);
            PendingIntent clickButton2=PendingIntent.getBroadcast(context,0,intent4,PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(modeID,clickButton2);
            boolean auto=AppSettings.loadWidget_auto(context,true);

            if(auto){
                System.out.println("オートモード");
                views.setCharSequence(R.id.widget_auto_btn,"setText","AUTO");
                Toast.makeText(context,"オートモードです",Toast.LENGTH_SHORT).show();
            }else{
                System.out.println("セルフモード");
                views.setCharSequence(R.id.widget_auto_btn,"setText","SELF");
            }

            if(!auto&&!AppSettings.comand){
                awm.updateAppWidget(awId, views);
                return;
            }

            AsyncTask<MyTweet,Void,MyTweet> task=new AsyncTask<MyTweet, Void, MyTweet>() {
                @Override
                protected MyTweet doInBackground(MyTweet... mt) {
                    try {
                        mt[0].twitter=TwitterUtils.getTwitterInstance(context);
                        mt[0].addWidgettimeline(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("エラー\n"+e.toString());
                        return null;
                    }
                    return mt[0];
                }

                @Override
                protected void onPostExecute(MyTweet mt) {
                    super.onPostExecute(mt);
                    if(mt!=null){
                        if(mt.tweets.size()>=4){
                            for(int i=0;i<tweetShow;i++){
                                String str="";
                                /*if(mt.tweets.get(i).RT){
                                    str+=mt.tweets.get(i).status.getUser().getName()+":RT\n";
                                }
                                str+=mt.tweets.get(i).name+"\n"+ mt.tweets.get(i).text;*/
                                str+=mt.tweets.get(i).text;
                                views.setTextViewText(TVids[i],str);
                                Intent intent2 = new Intent(context, OverlapService.class);
                                intent2.putExtra("text",str);
                                StatusSerialize ss=new StatusSerialize();
                                ss.setStatus(mt.tweets.get(i).status);
                                ss.setWidgetInfo(appWidgetId,TweetIds[i]);
                                intent2.putExtra("Status",ss);
                                PendingIntent pi=PendingIntent.getService(context,i,intent2,PendingIntent.FLAG_CANCEL_CURRENT);
                                views.setOnClickPendingIntent(TVids[i],pi);
                                //PendingIntent pi=PendingIntent.getBroadcast(context,0,intent,0);
                                twitter4j.Status status=mt.tweets.get(i).status;
                                boolean b1=status.isFavorited();
                                boolean b2=status.isRetweeted();
                                int color=colors[3];
                                if(b1&&b2)color=colors[0];
                                else if(b1)color=colors[1];
                                else if(b2)color=colors[2];
                                views.setInt(TweetIds[i],"setBackgroundColor",color);
                                awm.updateAppWidget(awId, views);
                                final int cov=i;
                                AsyncTask<MyTweet,Void, Bitmap> task2=new AsyncTask<MyTweet, Void, Bitmap>(){
                                    @Override
                                    protected Bitmap doInBackground(MyTweet... mt) {
                                        if(mt[0].tweets.get(cov).RT){
                                            try {
                                                return MyTweet.downloadImage(mt[0].tweets.get(cov).status.getRetweetedStatus().getUser().get400x400ProfileImageURLHttps(),60);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return null;
                                            }
                                        }else{
                                            try {
                                                return MyTweet.downloadImage(mt[0].tweets.get(cov).status.getUser().get400x400ProfileImageURLHttps(),60);
                                            } catch (Exception e) {
                                                return null;
                                            }
                                        }
                                    }
                                    @Override
                                    protected void onPostExecute(Bitmap bmp){
                                        views.setImageViewBitmap(IVids[cov],MyTweet.changeCircleBitmap(bmp));
                                        awm.updateAppWidget(awId, views);
                                    }
                                };
                                task2.execute(mt);
                            }

                        }

                    }
                }
            };

            mt.realwidth = 240;
            mt.realheight = 240;
            mt.showCount=8;
            mt.pagenum=1;
            task.execute(mt);
        }else{
            views.setTextViewText(R.id.textView10,"取得できません");
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        if(!enableUpdate)return;
        this.appWidgetManager=appWidgetManager;
        this.appWidgetIds=appWidgetIds;
        for (final int appWidgetId : appWidgetIds) {
            /*timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
            },0,INTERVAL_PERIOD);*/
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent){
        super.onReceive(context,intent);
        if(intent.getAction().equals("refresh")){
            System.out.println("更新します。");
            AppSettings.comand=true;
            updateAppWidget(context,AppWidgetManager.getInstance(context),intent.getIntExtra("ID",0));
            AppSettings.comand=false;
            intent.setAction("");
        }else if(intent.getAction().equals("mode_change")){
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
            if(AppSettings.loadWidget_auto(context,true)){
                System.out.println("手動に切り替えます");
                //AppSettings.setWidget_auto(false);
                AppSettings.storeWidget_auto(context,false);
                views.setCharSequence(R.id.widget_auto_btn,"setText","SELF");
            }else{
                System.out.println("自動に切り替えます");
                //AppSettings.setWidget_auto(true);
                AppSettings.storeWidget_auto(context,true);
                views.setCharSequence(R.id.widget_auto_btn,"setText","AUTO");
            }
            AppWidgetManager.getInstance(context).updateAppWidget(intent.getIntExtra("ID",0),views);
            intent.setAction("");
        } else{
            System.out.println("アラームセット");
            PendingIntent pi=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance(); // Calendar取得
            calendar.setTimeInMillis(System.currentTimeMillis()); // 現在時刻を取得
            calendar.add(Calendar.SECOND, INTERVAL_PERIOD); // 現時刻より120秒後を設定
            AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC,calendar.getTimeInMillis(),pi);
            AppSettings.alarmSet=true;
        }
    }



    @Override
    public void onEnabled(final Context context) {
        // Enter relevant functionality for when the first widget is created
        /*for (final int appWidgetId : appWidgetIds) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
            },0,INTERVAL_PERIOD);
        }*/
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}