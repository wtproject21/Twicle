package com.wt.project.twitter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class OverlapService extends Service {

    private WindowManager mWindowManager;
    private View mOverlapView;
    private WindowManager.LayoutParams mOverlapViewParams;
    Twitter twitter;
    Point realSize;
    Boolean[] favorited;
    Boolean[] retweeted;

    // Serviceに接続するためのBinderクラスを実装する
    public class LocalBinder extends Binder {
        //Serviceの取得
        OverlapService getService() {
            return OverlapService.this;
        }
    }
    // Binderの生成
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // Service接続時に呼び出される
        // 戻り値として、Serviceクラスとのbinderを返す。
        Log.i("", "onBind" + ": " + intent);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent){
        // Unbind後に再接続する場合に呼ばれる
        Log.i("", "onRebind" + ": " + intent);
    }

    @Override
    public boolean onUnbind(Intent intent){
        // Service切断時に呼び出される
        // onUnbindをreturn trueでoverrideすると次回バインド時にonRebildが呼ばれる
        return true;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        twitter=TwitterUtils.getTwitterInstance(getApplicationContext());

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mOverlapView = new FrameLayout(getApplicationContext());
        ((FrameLayout)mOverlapView).addView(layoutInflater.inflate(R.layout.tweet_pop, null));
        Button b=mOverlapView.findViewById(R.id.pop_tweet);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });
        Display disp = mWindowManager.getDefaultDisplay();

        realSize = new Point();
        disp.getRealSize(realSize);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            mOverlapViewParams = new WindowManager.LayoutParams(
                    realSize.x*4/5,
                    realSize.y/2,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.TRANSLUCENT);  // viewを透明にする
        }else{
            mOverlapViewParams = new WindowManager.LayoutParams(
                    realSize.x*4/5,
                    realSize.y/2,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.TRANSLUCENT);  // viewを透明にする
        }
        mOverlapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                stopSelf();
                return true;
            }
        });
        mOverlapViewParams.y=(-1)*realSize.y/10;
        mWindowManager.addView(mOverlapView, mOverlapViewParams);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        // to do something
        LinearLayout ll;
        ll=mOverlapView.findViewById(R.id.tweetShow);
        String text=intent.getStringExtra("text");
        final Context context=getApplicationContext();
        final Switch sw=mOverlapView.findViewById(R.id.pop_rep_switch);
        final EditText et=mOverlapView.findViewById(R.id.pop_tweet_text);
        final Button bt1=mOverlapView.findViewById(R.id.pop_favorite);
        final Button bt2=mOverlapView.findViewById(R.id.pop_retweet);
        final Button bt3=mOverlapView.findViewById(R.id.pop_tweet);
        final StatusSerialize ss=(StatusSerialize)intent.getSerializableExtra("Status");
        List<Status> statuses=new ArrayList<Status>();
        if(ss!=null){
            if(ss.isWidget){
                RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.new_app_widget);
                ss.views=views;
                ss.context=getApplicationContext();
            }//sw.setEnabled(true);
            statuses.add(ss.status);
            MyTweet mt=new MyTweet(getApplicationContext());
            mt.realheight=realSize.y/2;
            mt.realwidth=realSize.x*4/5;
            mt.asyncSetTweet_LinearLayout(getApplicationContext(),statuses,ll,true);
            final long twitterID=ss.status.getId();
            if(ss.status.isFavorited())bt1.setBackgroundColor(Color.argb(255,200,150,150));
            if(ss.status.isRetweeted())bt2.setBackgroundColor(Color.argb(255,150,150,200));
            final Boolean[] favorited = {ss.status.isFavorited()};
            final Boolean[] retweeted = {ss.status.isRetweeted()};
            this.favorited=favorited;
            this.retweeted=retweeted;
            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(favorited[0]){
                        AsyncTask<Void,Void,Integer> task=new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... aVoid) {
                                try {
                                    twitter.destroyFavorite(twitterID);
                                } catch (TwitterException e) {
                                    System.out.println(e.toString());
                                    return 1;
                                }
                                return 0;
                            }
                            @Override
                            protected void onPostExecute(Integer i){
                                bt1.setBackgroundColor(Color.argb(255,108,108,108));
                                ss.setWidgetBackgroundColor(-1);
                                favorited[0] =false;
                            }
                        };task.execute();
                    }else{
                        AsyncTask<Void,Void,Integer> task=new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... aVoid) {
                                try {
                                    twitter.createFavorite(twitterID);
                                } catch (TwitterException e) {
                                    System.out.println(e.toString());
                                    return 1;
                                }
                                return 0;
                            }
                            @Override
                            protected void onPostExecute(Integer i){
                                bt1.setBackgroundColor(Color.argb(255,200,150,150));
                                ss.setWidgetBackgroundColor(1);
                                favorited[0] = true;
                            }
                        };task.execute();
                    }
                }
            });
            bt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(retweeted[0]){
                        AsyncTask<Void,Void,Integer> task=new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... aVoid) {
                                try {
                                    twitter.unRetweetStatus(twitterID);
                                } catch (TwitterException e) {
                                    System.out.println(e.toString());
                                    return 1;
                                }
                                return 0;
                            }
                            @Override
                            protected void onPostExecute(Integer i){
                                bt2.setBackgroundColor(Color.argb(255,108,108,108));
                                ss.setWidgetBackgroundColor(-2);
                                retweeted[0]=false;
                            }
                        };task.execute();
                    }else{
                        AsyncTask<Void,Void,Integer> task=new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... aVoid) {
                                try {
                                    twitter.retweetStatus(twitterID);
                                } catch (TwitterException e) {
                                    System.out.println(e.toString());
                                    return 1;
                                }
                                return 0;
                            }
                            @Override
                            protected void onPostExecute(Integer i){
                                bt2.setBackgroundColor(Color.argb(255,150,150,200));
                                ss.setWidgetBackgroundColor(2);
                                retweeted[0]=true;
                            }
                        };task.execute();
                    }
                }
            });
            bt3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StatusUpdate statusUpdate;
                    Status status;
                    if(sw.isChecked()){
                        if(ss.status.isRetweet()){
                            status=ss.status.getRetweetedStatus();
                        }else{
                            status=ss.status;
                        }
                        statusUpdate=new StatusUpdate("@"+status.getUser().getScreenName()+" "+et.getText().toString());
                        statusUpdate.setInReplyToStatusId(status.getId());
                    }else{
                        status=ss.status;
                        statusUpdate=new StatusUpdate("@"+status.getUser().getScreenName()+" "+et.getText().toString());
                    }
                    AsyncTask<StatusUpdate,Void,Integer> task=new AsyncTask<StatusUpdate, Void, Integer>() {
                        @Override
                        protected Integer doInBackground(StatusUpdate... statusUpdate) {
                            try {
                                twitter.updateStatus(statusUpdate[0]);
                            } catch (TwitterException e) {
                                return 0;
                            }
                            return 1;
                        }
                        @Override
                        protected void onPostExecute(Integer integer) {
                            if(integer==0) Toast.makeText(context,"ツイートに失敗しました",Toast.LENGTH_SHORT).show();
                            else Toast.makeText(context,"ツイートしました",Toast.LENGTH_SHORT).show();
                        }
                    };task.execute(statusUpdate);
                }
            });
            System.out.println(ss.status.getText());
        }else{
            sw.setEnabled(false);
            bt1.setEnabled(false);
            bt2.setEnabled(false);
            bt3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StatusUpdate statusUpdate=new StatusUpdate(et.getText().toString());
                    AsyncTask<StatusUpdate,Void,Integer> task=new AsyncTask<StatusUpdate, Void, Integer>() {
                        @Override
                        protected Integer doInBackground(StatusUpdate... statusUpdate) {
                            try {
                                twitter.updateStatus(statusUpdate[0]);
                            } catch (TwitterException e) {
                                return 0;
                            }
                            return 1;
                        }
                        @Override
                        protected void onPostExecute(Integer integer) {
                            if(integer==0) Toast.makeText(context,"ツイートに失敗しました",Toast.LENGTH_SHORT).show();
                            else Toast.makeText(context,"ツイートしました",Toast.LENGTH_SHORT).show();
                        }
                    };task.execute(statusUpdate);
                }
            });
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // ServiceがunbindされるタイミングでViewも削除して上げる
        mWindowManager.removeView(mOverlapView);
        super.onDestroy();
    }


}