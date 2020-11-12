package com.wt.project.twitter;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;

public class MotionService extends Service implements SensorEventListener{
    private Context context;
    private SensorManager sensorManager;
    int sensorCountTotal=0,sensorCountSum=0;
    int sensorCountMax=50,sensorCountSumMax=4;
    double sensorBorder=2.0;
    int time=240;//240秒ごとに更新
    int showTweetCount=0;
    int addShowCount=1;
    float[] sensorLastAcc={0,0,0};
    MyTweet mt;
    List<Status> statuses=null;
    @Override
    public void onCreate(){
        super.onCreate();
        context=getApplicationContext();
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(sensor!=null){
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
        mt=new MyTweet(getApplicationContext());
        mt.twitter=TwitterUtils.getTwitterInstance(getApplicationContext());
        AsyncTask<MyTweet,Void,List<Status>> task=new AsyncTask<MyTweet, Void, List<Status>>() {
            @Override
            protected List<twitter4j.Status> doInBackground(MyTweet... myTweet) {
                Paging paging=new Paging(1,myTweet[0].showCount);
                //タイムラインの取得
                try {
                    return myTweet[0].twitter.getHomeTimeline(paging);
                } catch (TwitterException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<twitter4j.Status> status) {
                statuses=status;
            }
        };
        task.execute(mt);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 内部ストレージにログを保存
        /*InternalFileReadWrite fileReadWrite = new InternalFileReadWrite(context);
        fileReadWrite.writeFile();*/
        if(intent.getBooleanExtra("fromMe",false)){
            stopAlarmService(context);
            Toast.makeText(context,"サービスを終了しました。",Toast.LENGTH_SHORT).show();
            stopSelf();
            sensorManager.unregisterListener(this);
            return START_NOT_STICKY;
        }
        int requestCode = intent.getIntExtra("REQUEST_CODE",0);
        String channelId = "default";
        String title = context.getString(R.string.app_name);
        Intent intent1=new Intent(context,MotionService.class);
        intent1.putExtra("fromMe",true);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, requestCode,
                        intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        // ForegroundにするためNotificationが必要、Contextを設定
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        //final SensorManager sm=sensorManager;
        // Notification　Channel 設定
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(channelId, title , NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Silent Notification");
            // 通知音を消さないと毎回通知音が出てしまう
            // この辺りの設定はcleanにしてから変更
            channel.setSound(null,null);
            // 通知ランプを消す
            channel.enableLights(false);
            channel.setLightColor(Color.BLUE);
            // 通知バイブレーション無し
            channel.enableVibration(false);
        }
        if(notificationManager != null){
            String name="";
            String text="";
            if(statuses!=null){
                Status status;
                if(statuses.size()-showTweetCount-addShowCount>=0){
                    status=statuses.get(statuses.size()-showTweetCount-addShowCount);
                }else{
                    status=statuses.get(0);
                }
                if(status.isRetweet()){
                    name=status.getRetweetedStatus().getUser().getName();
                    text=status.getRetweetedStatus().getText();
                }else{
                    name=status.getUser().getName();
                    text=status.getText();
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(context, channelId)
                        .setContentTitle(title)
                        // android標準アイコンから
                        .setSmallIcon(android.R.drawable.btn_star)
                        .setContentText("タップするとサービスを終了します")
                        .setStyle(new Notification.BigTextStyle()
                                .setBigContentTitle(name)
                                .bigText(text)
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .build();
                // startForeground
                startForeground(1, notification);
            }else{
                Notification notification=new Notification.Builder(context)
                        .setContentTitle(title)
                        // android標準アイコンから
                        .setSmallIcon(android.R.drawable.btn_star)
                        .setContentText("タップするとサービスを終了します")
                        .setStyle(new Notification.BigTextStyle()
                                .setBigContentTitle(name)
                                .bigText(text)
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .build();
                notificationManager.notify(1,notification);
            }
        }

        // 毎回Alarmを設定する
        setNextAlarmService(context);
        if(statuses!=null){
            if(showTweetCount>=statuses.size()){
                showTweetCount=0;
                addShowCount=1;
                AsyncTask<MyTweet,Void,List<Status>> task=new AsyncTask<MyTweet, Void, List<Status>>() {
                    @Override
                    protected List<twitter4j.Status> doInBackground(MyTweet... myTweet) {
                        Paging paging=new Paging(1,myTweet[0].showCount);
                        //タイムラインの取得
                        try {
                            return myTweet[0].twitter.getHomeTimeline(paging);
                        } catch (TwitterException e) {
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(List<twitter4j.Status> status) {
                        statuses=status;
                    }
                };
                task.execute(mt);
            }else{
                showTweetCount++;
            }
        }

        return START_NOT_STICKY;
        //return START_STICKY;
        //return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        stopAlarmService(context);
    }

    // 次のアラームの設定
    private void setNextAlarmService(Context context){
        long repeatPeriod = (time/mt.showCount)*1000;
        Intent intent = new Intent(context, MotionService.class);
        long startMillis = System.currentTimeMillis() + repeatPeriod;
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null){
            // Android Oreo 以上を想定
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent);
            }else{
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,startMillis,pendingIntent);
            }
        }
    }

    private void stopAlarmService(Context context){
        Intent indent = new Intent(context, MotionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, indent, PendingIntent.FLAG_UPDATE_CURRENT);
        // アラームを解除する
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null){
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float force=0;
        for(int i=0;i<sensorEvent.values.length;i++){
            //System.out.println(sensorEvent.values[i]+" : "+i);
            float temp=sensorEvent.values[i]-sensorLastAcc[i];
            sensorLastAcc[i]=sensorEvent.values[i];
            force+=temp*temp;
        }
        force=(float)Math.sqrt(force);
        if(force>=sensorBorder){
            if(sensorCountSum>=sensorCountSumMax){
                System.out.println("動作を感知しました");
                sensorCountTotal=0;
                sensorCountSum=0;
                if(statuses!=null){
                    Status status;
                    if(statuses.size()-showTweetCount-addShowCount>=0){
                        status=statuses.get(statuses.size()-showTweetCount-addShowCount);
                        addShowCount++;
                    }else{
                        status=statuses.get(0);
                    }
                    StatusSerialize ss=new StatusSerialize();
                    ss.status=status;
                    ss.isWidget=false;
                    Intent intent=new Intent(context,OverlapService.class);
                    intent.putExtra("Status",ss);
                    startService(intent);
                }
                return;
            }else{
                sensorCountSum++;
            }
        }
        if(sensorCountTotal>=sensorCountMax){
            sensorCountTotal=0;
            sensorCountSum=0;
        }else{
            sensorCountTotal++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

class InternalFileReadWrite {

    private Context context;
    private String FILE_NAME = "log.txt";
    private StringBuffer stringBuffer;
    InternalFileReadWrite(Context context){
        this.context = context;
    }
    void clearFile(){
        // ファイル削除
        context.deleteFile(FILE_NAME);
        // StringBuffer clear
        stringBuffer.setLength(0);
    }
    // ファイルを保存
    void writeFile() {
        stringBuffer = new StringBuffer();
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat dataFormat = new SimpleDateFormat("hh:mm:ss", Locale.US);
        String cTime = dataFormat.format(currentTime);
        Log.d("debug", cTime);

        stringBuffer.append(cTime);
        stringBuffer.append(System.getProperty("line.separator"));// 改行

        // try-with-resources
        try (FileOutputStream fileOutputstream = context.openFileOutput(FILE_NAME, Context.MODE_APPEND)){
            fileOutputstream.write(stringBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ファイルを読み出し
    String readFile() {
        stringBuffer = new StringBuffer();
        // try-with-resources
        try (FileInputStream fileInputStream = context.openFileInput(FILE_NAME);
             BufferedReader reader= new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))
        ) {
            String lineBuffer;
            while( (lineBuffer = reader.readLine()) != null ) {
                stringBuffer.append(lineBuffer);
                stringBuffer.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }
}
