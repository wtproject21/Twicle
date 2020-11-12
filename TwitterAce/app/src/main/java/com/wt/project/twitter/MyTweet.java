package com.wt.project.twitter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class MyTweet {
    Twitter twitter;
    TextView tv;
    User user=null;
    String str="";
    int pagenum=1;
    LinearLayout ll;
    int showCount=30;
    int particle=3;
    ArrayList<Tweet> tweets;
    Context mainCon;
    int realwidth;
    int realheight;
    int lastTweet=0;

    MyTweet(Context context){
        tweets=new ArrayList<Tweet>();
        mainCon=context;
    }

    private static void storeAccessToken(long useId, AccessToken accessToken){
        //accessToken.getToken() を保存
        //accessToken.getTokenSecret() を保存
    }

    void login(){
        //do not use
        // このファクトリインスタンスは再利用可能でスレッドセーフです
        RequestToken requestToken = null;
        try {
            requestToken = twitter.getOAuthRequestToken();
        } catch (Exception e) {
            tv.setText("エラー1："+e.toString());
            return;
        }
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (null == accessToken) {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
            String pin = null;
            try {
                pin = br.readLine();
            } catch (IOException e) {
                tv.setText("エラー2");
                return;
            }
            try{
                if(pin.length() > 0){
                    accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                }else{
                    accessToken = twitter.getOAuthAccessToken();
                }
            } catch (TwitterException te) {
                if(401 == te.getStatusCode()){
                    System.out.println("Unable to get the access token.");
                }else{
                    te.printStackTrace();
                }
            }
        }
        //将来の参照用に accessToken を永続化する
        /*storeAccessToken((int) twitter.verifyCredentials().getId(), accessToken);
        Status status = twitter.updateStatus(args[0]);
        System.out.println("Successfully updated the status to [" + status.getText() + "].");
        System.exit(0);*/
    }

    void users() {
            str="";
            str+="user:"+user.getName()+"\n";
            str+="username:"+user.getScreenName()+"\n";
            str+="FriendCount:"+user.getFriendsCount()+"\n";
            str+="FollowersCount:"+user.getFollowersCount()+"\n";
            tv.setText(str);
            System.out.println(user.getName());
            System.out.println(user.getScreenName());
            System.out.println(user.getFriendsCount());
            System.out.println(user.getFollowersCount());
    }

    /**
     * List[Status]の内容をtweetsに加えるメソッド
     * 要非同期処理
     * @param context
     * @param statuses
     */
    void setTweet(Context context,List<Status> statuses,Boolean renew)throws Exception{
        if(renew){
            tweets.clear();
            lastTweet=0;
        }
        for(int k=0;k<statuses.size();k++){
            tweets.add(new Tweet(context));
            tweets.get(k+lastTweet).setStatus(statuses.get(k));
            tweets.get(k+lastTweet).setImageViews(context,statuses.get(k).getMediaEntities().length);
        }
        int i=lastTweet;
        for (Status status : statuses) {
            final int I=i;
            AsyncTask<Status,Void,Bitmap> task2=new AsyncTask<Status, Void, Bitmap>(){
                int cov=I ;
                @Override
                protected Bitmap doInBackground(twitter4j.Status... st) {
                    //System.out.println(st[0].getUser().get400x400ProfileImageURLHttps());
                    int i=0;
                    while(true){
                        try{
                            if(st[0].isRetweet()){
                                Bitmap rtIcon=changeCircleBitmap(downloadImage(st[0].getRetweetedStatus().getUser().get400x400ProfileImageURLHttps(),realwidth/5));
                                Bitmap icon=changeCircleBitmap(downloadImage(st[0].getUser().get400x400ProfileImageURLHttps(),realwidth/10));
                                Canvas canvas = new Canvas(rtIcon);
                                Paint p=new Paint();
                                p.setStyle(Paint.Style.STROKE);
                                p.setStrokeWidth(5);
                                p.setARGB(100,0,0,0);
                                canvas.drawBitmap(icon,canvas.getWidth()/2,canvas.getHeight()/2,null);
                                canvas.drawCircle(canvas.getWidth()*3/4,canvas.getHeight()*3/4,canvas.getWidth()/4,p);
                                return rtIcon;
                            }else{
                                return changeCircleBitmap(downloadImage(st[0].getUser().get400x400ProfileImageURLHttps(),realwidth/5));
                            }
                        }catch (Exception e){
                            System.out.println(e.toString());
                            try {
                                Thread.sleep(10000);
                                i++;
                                if(i==5)return null;
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                                return null;
                            }
                        }
                    }

                }
                @Override
                protected void onPostExecute(Bitmap bmp){
                    if(bmp!=null){
                        tweets.get(cov).icon.setImageBitmap(bmp);
                    }else{
                        System.out.println("Error");
                    }

                }
            };
            task2.execute(status);
            int j=0;
            for(MediaEntity me:status.getMediaEntities()){
                final  int J=j;
                if(me.getType().equals("photo")){
                    try {
                        AsyncTask<MediaEntity,Void,Bitmap> task=new AsyncTask<MediaEntity,Void,Bitmap>(){
                            int cov=I;
                            @Override
                            protected Bitmap doInBackground(MediaEntity... me) {
                                /*ImageView iv=new ImageView(context);
                                iv.setImageBitmap(downloadImage(me[0].getMediaURL()));;*/
                                int i=0;
                                while (true){
                                    try {
                                        return downloadImage(me[0].getMediaURL(),realwidth/particle);
                                    } catch (Exception e) {
                                        try {
                                            Thread.sleep(1000);
                                            i++;
                                            if(i==5)return null;
                                        } catch (InterruptedException ex) {
                                            ex.printStackTrace();
                                            return null;
                                        }
                                    }
                                }

                            }
                            @Override
                            protected void onPostExecute(Bitmap bmp){
                                if(bmp!=null){
                                    tweets.get(cov).ivs.get(J).setImageBitmap(bmp);
                                }else{
                                    System.out.println("Error");
                                }

                            }
                        };
                        task.execute(me);
                    } catch (Exception e) {

                    }
                }
                j++;
            }
            i++;
        }
    }

    /**
     * constantLaioutにLinarLayoutのようにツイートを配置するメソッド
     * @param constraintLayout
     */
    @SuppressLint("ResourceType")
    void setToConstraintLayout(final Context context,ConstraintLayout constraintLayout){
        for(int i=lastTweet;i<tweets.size();i++){
            ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
            if(constraintLayout.getChildCount()==0)params.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
            else params.topToBottom=constraintLayout.getChildAt(constraintLayout.getChildCount()-1).getId();
            params.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
            if(tweets.get(i).status.isRetweet()){
                TextView tv1=new TextView(context);
                tv1.setText(tweets.get(i).status.getUser().getName()+"さんがリツイートしました。");
                tv1.setTextColor(Color.argb(200,200,200,200));
                tv1.setBackgroundColor(Color.argb(200,100,100,100));
                tv1.setId(200+i);
                constraintLayout.addView(tv1,params);
                params=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
                params.topToBottom=200+i;
            }
            tweets.get(i).setLayout(realwidth,realheight);
            tweets.get(i).tv.setText(tweets.get(i).name+"\n\n"+tweets.get(i).text+"\n");
            tweets.get(i).tweetLayout.setId(300+i);
            final int I=i;
            tweets.get(i).tweetLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent2=new Intent(mainCon,OverlapService.class);
                    StatusSerialize ss=new StatusSerialize();
                    ss.status=tweets.get(I).status;
                    intent2.putExtra("Status",ss);
                    mainCon.startService(intent2);
                }
            });
            constraintLayout.addView(tweets.get(i).tweetLayout,params);
            params=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
            params.topToBottom=300+i;
            TextView tv2=new TextView(context);
            tv2.setText(" ");
            tv2.setBackgroundColor(Color.argb(100,255,255,255));
            tv2.setId(400+i);
            constraintLayout.addView(tv2,params);
        }
        lastTweet=tweets.size();
    }

    /**
     * 自動でConstantLayoutにStatusの内容を入れるメソッド
     * @param context
     * @param statuses
     * @param constraintLayout
     * @param renew
     * 要素をすべて消して新しく作り直す
     */
    void asyncSetTweet_ConstraintLayout(final Context context, final List<Status> statuses, final ConstraintLayout constraintLayout, final boolean renew){
        AsyncTask<MyTweet,Void,MyTweet> task=new AsyncTask<MyTweet, Void, MyTweet>() {
            @Override
            protected MyTweet doInBackground(MyTweet... myTweet) {
                try {
                    myTweet[0].setTweet(context,statuses,renew);
                } catch (Exception e) {
                    return null;
                }
                return myTweet[0];
            }
            @Override
            protected  void onPostExecute(MyTweet myTweet){
                if(myTweet!=null)myTweet.setToConstraintLayout(context,constraintLayout);
            }
        };
        task.execute(this);
    }

    void asyncSetTweet_LinearLayout(final Context context, final List<Status> statuses, final LinearLayout linearLayout, final boolean renew){
        AsyncTask<MyTweet,Void,MyTweet> task=new AsyncTask<MyTweet,Void,MyTweet>(){
            @Override
            protected MyTweet doInBackground(MyTweet... mytweet){
                try{
                    mytweet[0].setTweet(context,statuses,renew);
                }catch (Exception e){
                    return null;
                }
                return mytweet[0];
            }
            @Override
            protected void onPostExecute(MyTweet myTweet){
                if(myTweet!=null)myTweet.setLinarLayout(context,linearLayout);
            }
        };
        task.execute(this);
    }
    /**
     * ツイッターのタイムラインを追加するメソッド
     * @param context
     * @throws Exception
     */
    void addtimeline(final Context context)throws Exception{
        Paging paging=new Paging(pagenum,showCount);
        //タイムラインの取得
        List<Status> statuses = twitter.getHomeTimeline(paging);
        System.out.println("Showing home timeline.");
        for(int k=0;k<statuses.size();k++){
            tweets.add(new Tweet(context));
            tweets.get(k+lastTweet).setStatus(statuses.get(k));
            tweets.get(k+lastTweet).setImageViews(context,statuses.get(k).getMediaEntities().length);
        }
        int i=lastTweet;
        for (Status status : statuses) {
            final int I=i;
            AsyncTask<Status,Void,Bitmap> task2=new AsyncTask<Status, Void, Bitmap>(){
                int cov=I ;
                @Override
                protected Bitmap doInBackground(twitter4j.Status... st) {
                    //System.out.println(st[0].getUser().get400x400ProfileImageURLHttps());
                    int i=0;
                    while(true){
                        try{
                            if(st[0].isRetweet()){
                                Bitmap rtIcon=changeCircleBitmap(downloadImage(st[0].getRetweetedStatus().getUser().get400x400ProfileImageURLHttps(),realwidth/5));
                                Bitmap icon=changeCircleBitmap(downloadImage(st[0].getUser().get400x400ProfileImageURLHttps(),realwidth/10));
                                Canvas canvas = new Canvas(rtIcon);
                                Paint p=new Paint();
                                p.setStyle(Paint.Style.STROKE);
                                p.setStrokeWidth(5);
                                p.setARGB(100,0,0,0);
                                canvas.drawBitmap(icon,canvas.getWidth()/2,canvas.getHeight()/2,null);
                                canvas.drawCircle(canvas.getWidth()*3/4,canvas.getHeight()*3/4,canvas.getWidth()/4,p);
                                return rtIcon;
                            }else{
                                return changeCircleBitmap(downloadImage(st[0].getUser().get400x400ProfileImageURLHttps(),realwidth/5));
                            }
                        }catch (Exception e){
                            System.out.println(e.toString());
                            try {
                                Thread.sleep(10000);
                                i++;
                                if(i==5)return null;
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                                return null;
                            }
                        }
                    }

                }
                @Override
                protected void onPostExecute(Bitmap bmp){
                    if(bmp!=null){
                        tweets.get(cov).icon.setImageBitmap(bmp);
                    }else{
                        System.out.println("Error");
                    }

                }
            };
            task2.execute(status);
            int j=0;
            for(MediaEntity me:status.getMediaEntities()){
                final  int J=j;
                if(me.getType().equals("photo")){
                    try {
                        AsyncTask<MediaEntity,Void,Bitmap> task=new AsyncTask<MediaEntity,Void,Bitmap>(){
                            int cov=I;
                            @Override
                            protected Bitmap doInBackground(MediaEntity... me) {
                                /*ImageView iv=new ImageView(context);
                                iv.setImageBitmap(downloadImage(me[0].getMediaURL()));;*/
                                int i=0;
                                while (true){
                                    try {
                                        return downloadImage(me[0].getMediaURL(),realwidth/particle);
                                    } catch (Exception e) {
                                        try {
                                            Thread.sleep(1000);
                                            i++;
                                            if(i==5)return null;
                                        } catch (InterruptedException ex) {
                                            ex.printStackTrace();
                                            return null;
                                        }
                                    }
                                }

                            }
                            @Override
                            protected void onPostExecute(Bitmap bmp){
                                if(bmp!=null){
                                    tweets.get(cov).ivs.get(J).setImageBitmap(bmp);
                                }else{
                                    System.out.println("Error");
                                }

                            }
                        };
                        task.execute(me);
                    } catch (Exception e) {

                    }
                }
                j++;
            }
            i++;
        }
        pagenum++;
    }
    void addWidgettimeline(final Context context)throws Exception{
        Paging paging=new Paging(pagenum,showCount);
        //タイムラインの取得
        List<Status> statuses = twitter.getHomeTimeline(paging);
        System.out.println("Showing home timeline.");
        for(int k=0;k<statuses.size();k++){
            tweets.add(new Tweet(context));
            tweets.get(k+lastTweet).setStatus(statuses.get(k));
            //tweets.get(k+lastTweet).setImageViews(context,statuses.get(k).getMediaEntities().length);
        }
    }

    /**
     * tweetsからlinarlayoutにツイッターのレイアウトで加える
     */
    void setLinarLayout(){
        for(int i=lastTweet;i<tweets.size();i++){
            if(tweets.get(i).status.isRetweet()){
                TextView tv1=new TextView(mainCon);
                tv1.setText(tweets.get(i).status.getUser().getName()+"さんがリツイートしました。");
                tv1.setTextColor(Color.argb(200,200,200,200));
                tv1.setBackgroundColor(Color.argb(100,100,100,100));
                ll.addView(tv1);
            }
            tweets.get(i).setLayout(realwidth,realheight);
            tweets.get(i).tv.setText(tweets.get(i).name+"\n\n"+tweets.get(i).text+"\n");
            final int I=i;
            tweets.get(i).tweetLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent2=new Intent(mainCon,OverlapService.class);
                    StatusSerialize ss=new StatusSerialize();
                    ss.status=tweets.get(I).status;
                    //View[] v={tweets.get(I).tweetLayout};
                    //ss.v=v;
                    intent2.putExtra("Status",ss);
                    //intent2.putExtra("text",tweets.get(I).name+"\n"+tweets.get(I).text);
                    mainCon.startService(intent2);
                }
            });
            ll.addView(tweets.get(i).tweetLayout);
            TextView tv2=new TextView(mainCon);
            tv2.setText(" ");
            tv2.setBackgroundColor(Color.argb(100,255,255,255));
            ll.addView(tv2);
        }
        lastTweet=tweets.size();
    }

    void setLinarLayout(final Context context, LinearLayout ll){
        for(int i=lastTweet;i<tweets.size();i++){
            if(tweets.get(i).status.isRetweet()){
                TextView tv1=new TextView(context);
                tv1.setText(tweets.get(i).status.getUser().getName()+"さんがリツイートしました。");
                tv1.setTextColor(Color.argb(200,200,200,200));
                tv1.setBackgroundColor(Color.argb(100,100,100,100));
                ll.addView(tv1);
            }
            tweets.get(i).setLayout(realwidth,realheight);
            tweets.get(i).tv.setText(tweets.get(i).name+"\n\n"+tweets.get(i).text+"\n");
            ll.addView(tweets.get(i).tweetLayout);
            /*TextView tv2=new TextView(context);
            tv2.setText(" ");
            tv2.setBackgroundColor(Color.argb(100,255,255,255));
            ll.addView(tv2);*/
        }
        lastTweet=tweets.size();
    }



    Bitmap roadBitmap (Context context,String url,int maxsize)throws Exception{
        //https://qiita.com/exilias/items/38075e08ca45d223cf92
        Uri uri=Uri.parse(url);
        InputStream stream = context.getContentResolver().openInputStream(uri);
        // 画像サイズ情報を取得する
        BitmapFactory.Options imageOptions = new BitmapFactory.Options();
        imageOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, imageOptions);
        stream.close();
        // もし、画像が大きかったら縮小して読み込む
        //  今回はimageSizeMaxの大きさに合わせる
        Bitmap bitmap;
        int imageSizeMax = maxsize;
        stream = context.getContentResolver().openInputStream(uri);
        float imageScaleWidth = (float)imageOptions.outWidth / imageSizeMax;
        float imageScaleHeight = (float)imageOptions.outHeight / imageSizeMax;

        // もしも、縮小できるサイズならば、縮小して読み込む
        if (imageScaleWidth > 2 && imageScaleHeight > 2) {
            BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();

            // 縦横、小さい方に縮小するスケールを合わせる
            int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));

            // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
            for (int i = 2; i <= imageScale; i *= 2) {
                imageOptions2.inSampleSize = i;
            }

            bitmap = BitmapFactory.decodeStream(stream, null, imageOptions2);
        } else {
            bitmap = BitmapFactory.decodeStream(stream);
        }
        stream.close();
        return bitmap;
    }

    BitmapFactory.Options optionSetting(InputStream is,int maxsize){
        // 画像サイズ情報を取得する
        /*BitmapFactory.Options imageOptions = new BitmapFactory.Options();
        imageOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, imageOptions);
        int imageSizeMax = maxsize;
        float imageScaleWidth = (float)(imageOptions.outWidth / imageSizeMax);
        float imageScaleHeight = (float)(imageOptions.outHeight) / imageSizeMax;
        System.out.println((imageScaleHeight)+":"+imageScaleWidth+"\n");
        if (imageScaleWidth > 2 && imageScaleHeight > 2) {
            BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();
            // 縦横、小さい方に縮小するスケールを合わせる
            int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));
            // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
            for (int i = 2; i <= imageScale; i *= 2) {
                imageOptions2.inSampleSize = i;
            }
            return imageOptions2;
        }*/
        BitmapFactory.Options normal=new BitmapFactory.Options();
        return normal;
    }

    Bitmap downloadImage(String address) {
    //https://akira-watson.com/android/httpurlconnection-get.html
    Bitmap bmp = null;

    HttpURLConnection urlConnection = null;

    try {
        URL url = new URL( address );

        // HttpURLConnection インスタンス生成
        urlConnection = (HttpURLConnection) url.openConnection();

        // タイムアウト設定
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(20000);

        // リクエストメソッド
        urlConnection.setRequestMethod("GET");

        // リダイレクトを自動で許可しない設定
        urlConnection.setInstanceFollowRedirects(false);

        // ヘッダーの設定(複数設定可能)
        urlConnection.setRequestProperty("Accept-Language", "jp");

        // 接続
        urlConnection.connect();

        int resp = urlConnection.getResponseCode();

        switch (resp){
            case HttpURLConnection.HTTP_OK:
                try(InputStream is = urlConnection.getInputStream()){
                    BitmapFactory bf=new BitmapFactory();
                    BitmapFactory.Options bmpOption=new BitmapFactory.Options();
                    bmpOption.inJustDecodeBounds=true;
                    int maxsize=200;
                    float f=1;
                    if(bmpOption.outWidth>bmpOption.outHeight){
                        if(bmpOption.outWidth>maxsize){
                            f=bmpOption.outWidth/maxsize;
                        }
                    }else{
                        if(bmpOption.outHeight>maxsize){
                            f=bmpOption.outHeight/maxsize;
                        }
                    }
                    for(int i=1;i<Integer.MAX_VALUE;i*=2){
                        if(f<=i){
                            bmpOption.inSampleSize=i;
                            break;
                        }
                    }
                    bmpOption.inJustDecodeBounds=false;
                    bmp = BitmapFactory.decodeStream(is,null,bmpOption);
                    is.close();
                } catch(IOException e){
                    System.out.println(e.toString());
                    e.printStackTrace();
                }
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                break;
            default:
                break;
        }
        System.out.println("画像取得成功");
    } catch (Exception e) {
        System.out.println("失敗");
        Log.d("debug", "downloadImage error");
        e.printStackTrace();
    } finally {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    return bmp;
}


    public static Bitmap downloadImage(String address,int maxsize)throws Exception {
        //https://akira-watson.com/android/httpurlconnection-get.html
        Bitmap bmp = null;

        HttpURLConnection urlConnection = null;
        HttpURLConnection urlConnection2 = null;
        try {
            URL url = new URL( address );
            URL url2 = new URL( address );

            // HttpURLConnection インスタンス生成
            urlConnection2 = (HttpURLConnection) url2.openConnection();

            // タイムアウト設定
            urlConnection2.setReadTimeout(10000);
            urlConnection2.setConnectTimeout(20000);

            // リクエストメソッド
            urlConnection2.setRequestMethod("GET");

            // リダイレクトを自動で許可する設定
            urlConnection2.setInstanceFollowRedirects(true);

            // ヘッダーの設定(複数設定可能)
            urlConnection2.setRequestProperty("Accept-Language", "jp");

            // 接続
            urlConnection2.connect();

            // HttpURLConnection インスタンス生成
            urlConnection = (HttpURLConnection) url.openConnection();

            // タイムアウト設定
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(20000);

            // リクエストメソッド
            urlConnection.setRequestMethod("GET");

            // リダイレクトを自動で許可する設定
            urlConnection.setInstanceFollowRedirects(true);

            // ヘッダーの設定(複数設定可能)
            urlConnection.setRequestProperty("Accept-Language", "jp");

            // 接続
            urlConnection.connect();

            int resp = urlConnection.getResponseCode();

            switch (resp){
                case HttpURLConnection.HTTP_OK:
                    try{
                        InputStream is = urlConnection.getInputStream();
                        InputStream test = urlConnection2.getInputStream();
                        BitmapFactory bf=new BitmapFactory();
                        BitmapFactory.Options bmpOption=new BitmapFactory.Options();
                        bmpOption.inJustDecodeBounds=true;
                        BitmapFactory.decodeStream(test,null,bmpOption);
                        float f=1;
                        if(bmpOption.outWidth>bmpOption.outHeight){
                            if(bmpOption.outWidth>maxsize){
                                f=(float) bmpOption.outWidth/maxsize;
                            }
                        }else{
                            if(bmpOption.outHeight>maxsize){
                                f=(float)bmpOption.outHeight/maxsize;
                            }
                        }
                        for(int i=1;i<Integer.MAX_VALUE;i*=2){
                            if(f<=i){
                                bmpOption.inSampleSize=i;
                                break;
                            }
                        }
                        bmpOption.inJustDecodeBounds=false;
                        bmpOption.inPreferredConfig= Bitmap.Config.ARGB_8888;
                        bmp = BitmapFactory.decodeStream(is,null,bmpOption);
                        is.close();
                    } catch(Exception e){
                        System.out.println(e.toString());
                        e.printStackTrace();
                        throw e;
                    }
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    break;
                default:
                    break;
            }
            //System.out.println("画像取得成功");
        } catch (Exception e) {
            System.out.println("失敗");
            Log.d("debug", "downloadImage error");
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return bmp;
    }

    public static Bitmap changeCircleBitmap(Bitmap bmp){
        Bitmap result=bmp.copy(Bitmap.Config.ARGB_8888,true);
        result.setHasAlpha(true);
        //System.out.println(result.hasAlpha()+" : "+result.getConfig());
        int x=bmp.getWidth()/2;
        int y=bmp.getHeight()/2;
        int r=bmp.getWidth()<bmp.getHeight()?bmp.getWidth()/2:bmp.getHeight()/2;
        for(int i=0;i<bmp.getWidth();i++){
            for(int j=0;j<bmp.getHeight();j++){
                if(r(i-x,j-y)>r){
                    result.setPixel(i,j,0);
                    //System.out.println(result.getPixel(i,j)+" : "+Color.argb(0,0,0,0)+" : "+result.hasAlpha());
                }
            }
        }
        return result;
    }

    static double r(double x, double y){
        return Math.sqrt(x*x+y*y);
    }

}




class Tweet{
    boolean RT=false;
    Status status;
    TextView tv;
    TextView timeView;
    ImageView iv;
    ArrayList<ImageView> ivs;
    String name;
    String text;
    ImageView icon;
    Bitmap bmpIcon;
    RelativeLayout tweetLayout;
    Calendar times;
    int particle=3;
    Tweet(Context context){
        iv=new ImageView(context);
        icon=new ImageView(context);
        tv=new TextView(context);
        ivs=new ArrayList<ImageView>();
        tweetLayout=new RelativeLayout(context);
        times=Calendar.getInstance();
        timeView=new TextView(context);
    }
    TextView getTextView(){ return tv; }
    ImageView getImageView(){return iv;}
    void setImageViews(Context context,int n){
        for(int i=0;i<n;i++){
            ivs.add(new ImageView(context));
        }
    }
    void setStatus(Status status){
        RT=status.isRetweet();
        this.status=status;
        if(RT){
            this.name=status.getRetweetedStatus().getUser().getName();
            times.setTime(status.getRetweetedStatus().getCreatedAt());
            this.text=status.getRetweetedStatus().getText();
        }else{
            this.name=status.getUser().getName();
            times.setTime(status.getCreatedAt());
            this.text=status.getText();
        }
    }
    @SuppressLint("ResourceType")
    void setLayout(){
        icon.setId(1);
        tv.setId(2);
        tv.setBackgroundColor(Color.rgb(200,200,200));


        RelativeLayout.LayoutParams params;
        params=new RelativeLayout.LayoutParams(60,60);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tweetLayout.addView(icon,params);
        params=new RelativeLayout.LayoutParams(180,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.LEFT_OF,1);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        tweetLayout.addView(tv,params);
        params=new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.BELOW,2);
        tweetLayout.addView(iv,100,100);

    }
    @SuppressLint("ResourceType")
    void setLayout(int realwidth,int realheight){
        icon.setId(1);
        tv.setId(2);
        tv.setTextColor(Color.argb(210,255,255,255));


        RelativeLayout.LayoutParams params;
        params=new RelativeLayout.LayoutParams(realwidth/5,realwidth/5);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tweetLayout.addView(icon,params);
        params=new RelativeLayout.LayoutParams(180,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.LEFT_OF,1);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        tweetLayout.addView(tv,params);
        for(int i=0;i<ivs.size();i++){
            ivs.get(i).setId(i+3);
            params=new RelativeLayout.LayoutParams(realwidth/particle,realwidth/particle);
            switch (i%3){
                case 0:
                    params.addRule(RelativeLayout.BELOW,2+i);
                    break;
                case 1:
                case 2:
                    params.addRule(RelativeLayout.BELOW,2+i-i%3);
                    params.addRule(RelativeLayout.RIGHT_OF,2+i);
            }
            //params.addRule(RelativeLayout.BELOW,2+i);
            tweetLayout.addView(ivs.get(i),params);
        }
        params=new RelativeLayout.LayoutParams(-2,-2);
        params.addRule(RelativeLayout.BELOW,1);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        timeView.setText(showTime());
        tweetLayout.addView(timeView,params);
    }
    @SuppressLint("ResourceType")
    void setLayout(int realwidth,int realheight,int color){
        icon.setId(1);
        tv.setId(2);
        tv.setTextColor(color);


        RelativeLayout.LayoutParams params;
        params=new RelativeLayout.LayoutParams(realwidth/5,realwidth/5);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tweetLayout.addView(icon,params);
        params=new RelativeLayout.LayoutParams(180,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.LEFT_OF,1);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        tweetLayout.addView(tv,params);
        for(int i=0;i<ivs.size();i++){
            ivs.get(i).setId(i+3);
            params=new RelativeLayout.LayoutParams(realwidth/particle,realwidth/particle);
            switch (i%3){
                case 0:
                    params.addRule(RelativeLayout.BELOW,2+i);
                    break;
                case 1:
                case 2:
                    params.addRule(RelativeLayout.BELOW,2+i-i%3);
                    params.addRule(RelativeLayout.RIGHT_OF,2+i);
            }
            //params.addRule(RelativeLayout.BELOW,2+i);
            tweetLayout.addView(ivs.get(i),params);
        }
        params=new RelativeLayout.LayoutParams(-2,-2);
        params.addRule(RelativeLayout.BELOW,1);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        timeView.setText(showTime());
        tweetLayout.addView(timeView,params);
    }
    String showTime(){
        return String.format("%02d : %02d",times.get(Calendar.HOUR_OF_DAY),times.get(Calendar.MINUTE));
    }
}
