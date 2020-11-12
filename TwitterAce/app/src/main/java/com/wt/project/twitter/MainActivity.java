package com.wt.project.twitter;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMIISSION = 1;
    MyTweet mt;

    //非同期タスク宣言
    private AsyncJob asynctask;

    // Serviceとのインターフェースクラス
    private ServiceConnection mConnection = new ServiceConnection() {
        OverlapService mBindService;
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Serviceとの接続確立時に呼び出される。
            // service引数には、Onbind()で返却したBinderが渡される
            mBindService = ((OverlapService.LocalBinder)service).getService();
            //必要であればmBoundServiceを使ってバインドしたServiceへの制御を行う
        }

        public void onServiceDisconnected(ComponentName className) {
            // Serviceとの切断時に呼び出される。
            mBindService = null;
        }
    };

    private void regetToken(){
        TwitterUtils.removeAccessToken(this);
        Intent intent = new Intent(getApplication(), TwitterOAuthActivity.class);
        startActivity(intent);
        finish();
    }

    // 非同期処理を開始する
    private void asynctask_job(Twitter tw){
        final AsyncJob asynctask = new AsyncJob(this);
        asynctask.execute(tw);
    }

    //onPostExecuteで実行される関数
    public void result_job(User user){
        try {
            mt.user=user;
            mt.users();
        } catch (Exception e) {
            regetToken();
        }
    }

    {
        try {
            mt = new MyTweet(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //SecondActivityから戻ってきた場合
            case (REQUEST_CODE_OVERLAY_PERMIISSION):
                if(!(Build.VERSION.SDK_INT<Build.VERSION_CODES.M||Settings.canDrawOverlays(this))){
                    finish();
                }
        }
    }

    void dopermission(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M||Settings.canDrawOverlays(this))return;
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:"+getPackageName())
        );
        startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMIISSION);
    }

        @Override
        protected void onCreate (Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            Intent intent;
            if (!TwitterUtils.hasAccessToken(this)) {
                intent = new Intent(getApplication(), TwitterOAuthActivity.class);
                startActivity(intent);
                finish();
            }
            Intent inte=new Intent(getApplicationContext(),MotionService.class);
            startService(inte);

            setContentView(R.layout.activity_main);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AsyncTask<MyTweet, Void, MyTweet> task = new AsyncTask<MyTweet, Void, MyTweet>() {
                        int error = 0;
                        int time;

                        @Override
                        protected MyTweet doInBackground(MyTweet... mt) {
                            try {
                                //タイムラインの取得
                                mt[0].addtimeline(mt[0].mainCon);
                            } catch (TwitterException te) {
                                if (429 == te.getStatusCode()) {
                                    System.out.println("Unable to get the access token.");
                                    error = 429;
                                    time = te.getRateLimitStatus().getSecondsUntilReset();
                                } else {
                                    System.out.println(te.toString());
                                }
                                return null;
                            } catch (Exception e) {
                                System.out.println(e.toString());
                                return null;
                            }
                            return mt[0];
                        }

                        @Override
                        protected void onPostExecute(MyTweet mt) {
                            if (mt == null) {
                                if (error == 429) {
                                    String str = "使用可能まであと";
                                    if (time / 60 != 0) str += time / 60 + "分";
                                    if (time % 60 != 0) str += time % 60 + "秒";
                                    showToast("現在タイムラインが取得できません。\nしばらく時間をおいてから使用してください。\n" + str);
                                }
                                showToast("エラーが発生しました。");
                            } else {
                                //レイアウトにセット
                                mt.setLinarLayout();
                                /*mt.tweets.get(cov).tv.setText(mt.tweets.get(cov).name+"\n"+mt.tweets.get(cov).text+"\n");
                                mt.ll.addView(mt.tweets.get(cov).icon);
                                mt.ll.addView(mt.tweets.get(cov).iv);*/
                                //枠

                            }
                        }
                    };
                    task.execute(mt);
                    AsyncTask<MyTweet, Void, ImageView> task2 = new AsyncTask<MyTweet, Void, ImageView>() {
                        @Override
                        protected ImageView doInBackground(MyTweet... mt) {
                            ImageView iv = null;
                            try {
                                iv = new ImageView(getApplicationContext());
                                iv.setImageBitmap(downloadImage("http://a1.mzstatic.com/us/r30/Purple/v4/d8/6a/93/d86a9356-1ed7-bc28-10e0-02ad1d03a683/mzl.yafphczs.png"));

                            } catch (Exception e) {
                                System.out.println("出力不可" + e.toString());
                            }
                            return iv;
                        }

                        @Override
                        protected void onPostExecute(ImageView iv) {
                            LinearLayout ll = findViewById(R.id.linearlayout1);
                            // 元画像の1/2の縦横幅にする
                            int imageWidth = 50;
                            int imageHeight = 50;

                            // 画像の縦横サイズをimageViewのサイズとして設定
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);
                            iv.setLayoutParams(layoutParams);
                            ll.addView(iv);
                            mt.tv.setText(mt.str);
                        }
                    };
                }
            });
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display disp = wm.getDefaultDisplay();

            Point realSize = new Point();
            disp.getRealSize(realSize);

            mt.realwidth = realSize.x;
            mt.realheight = realSize.y;

            dopermission();
        }

        @Override
        protected void onResume () {
            super.onResume();


        }

        @Override
        protected void onStart () {
            super.onStart();
            mt.tv = findViewById(R.id.textView);
            mt.ll = findViewById(R.id.linearlayout1);
            mt.twitter = TwitterUtils.getTwitterInstance(this);
            TextView tv2 = new TextView(this);
            tv2.setText(" ");
            tv2.setBackgroundColor(Color.argb(100, 255, 255, 255));
            mt.ll.addView(tv2);
            asynctask_job(mt.twitter);
        }

        @Override
        protected void onPause () {
            super.onPause();
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            if (id == R.id.app_bar_search) {
                Intent intent = new Intent(getApplication(), SearchActivity.class);
                startActivity(intent);
            }

            //noinspection SimplifiableIfStatement
            if (id == R.id.tweet_menu) {
                Intent intent2 = new Intent(MainActivity.this, OverlapService.class);
                intent2.putExtra("replay",false);
                startService(intent2);
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        protected void onDestroy () {
            super.onDestroy();
            // Serviceをunbindする
            Intent inte=new Intent(getApplicationContext(),MotionService.class);
            stopService(inte);
        }

        //トーストを表示するメソッド
        private void showToast (String text){
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }

        Bitmap downloadImage (String address){
            //https://akira-watson.com/android/httpurlconnection-get.html
            Bitmap bmp = null;

            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(address);

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

                switch (resp) {
                    case HttpURLConnection.HTTP_OK:
                        try (InputStream is = urlConnection.getInputStream()) {
                            bmp = BitmapFactory.decodeStream(is);
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        break;
                    default:
                        break;
                }
                System.out.println("成功");
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
    }
