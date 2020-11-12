package com.wt.project.twitter;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class TweetPopActivity extends AppCompatActivity {
    private WindowManager mWindowManager;
    private FrameLayout mOverlapView;
    private WindowManager.LayoutParams mOverlapViewParams;
    Button tweet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweet_pop);
        /*mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mOverlapView = new FrameLayout(getApplicationContext());
        ((FrameLayout)mOverlapView).addView(layoutInflater.inflate(R.layout.tweet_pop, null));
        mOverlapViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,       // アプリケーションのTOPに配置
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |  // フォーカスを当てない(下の画面の操作がd系なくなるため)
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |        // OverlapするViewを全画面表示
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // モーダル以外のタッチを背後のウィンドウへ送信
                PixelFormat.TRANSLUCENT);  // viewを透明にする
        mWindowManager.addView(mOverlapView, mOverlapViewParams);
        tweet=findViewById(R.id.button2);
        tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWindowManager.removeView(mOverlapView);
                finish();
            }
        });*/
    }
}
