package com.wt.project.twitter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;

public class SearchActivity extends AppCompatActivity {
    EditText atName;
    EditText searchQuery;
    Button search;
    Switch mediaOnly;
    TwitterFunction twitterFunction;
    ConstraintLayout constraintLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        if(!TwitterUtils.hasAccessToken(this)){
            intent = new Intent(getApplication(), TwitterOAuthActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.search_view);
        searchQuery=findViewById(R.id.editTextTextPersonName);
        atName=findViewById(R.id.editTextTextPersonName2);
        search=findViewById(R.id.button);
        mediaOnly=findViewById(R.id.switch1);
        constraintLayout=findViewById(R.id.resultLayout);
        twitterFunction=new TwitterFunction(TwitterUtils.getTwitterInstance(this));

        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();

        final Point realSize = new Point();
        disp.getRealSize(realSize);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str="";
                //@sasakisaki9646
                if(!atName.getText().toString().equals("")){
                    str+=str+="from:@"+atName.getText().toString()+" ";
                }
                if(!searchQuery.getText().toString().equals("検索")){
                    str+=searchQuery.getText().toString();
                }
                if(mediaOnly.isChecked()){
                    str+=TwitterFunction.filter_media;
                }
                constraintLayout.removeAllViews();

                AsyncTask<String,Void,List<Status>> task=new AsyncTask<String, Void, List<Status>>() {
                    @Override
                    protected List<twitter4j.Status> doInBackground(String... string) {
                        return twitterFunction.searchTwittter(string[0]);
                    }
                    @Override
                    protected void onPostExecute(List<twitter4j.Status> list){
                        if(list!=null){
                            final List<twitter4j.Status> result=list;
                            final int margin=3;
                            if(mediaOnly.isChecked()){
                                constraintLayout.setBackgroundColor(Color.argb(0,255,255,255));
                                int i=0;
                                for(twitter4j.Status re:result){
                                    for(MediaEntity mediae:re.getMediaEntities()){
                                        ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(realSize.x/margin,realSize.y/margin);
                                        if(i==0){
                                            params.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
                                            params.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
                                        }else if(i%margin!=0){
                                            params.topToTop=i+100-1;
                                            params.leftToRight=i+100-1;
                                        }else{
                                            params.topToBottom=i+100-1;
                                            params.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
                                        }
                                        ImageView iv=new ImageView(getApplicationContext());
                                        iv.setId(i+100);
                                        constraintLayout.addView(iv,params);
                                        final int I=i;
                                        final ImageView Ima=iv;
                                        AsyncTask<twitter4j.MediaEntity,Void, Bitmap> task2=new AsyncTask<MediaEntity, Void, Bitmap>() {
                                            int ID=I+100;
                                            ImageView iv=Ima;
                                            @Override
                                            protected Bitmap doInBackground(MediaEntity... mediaEntity) {
                                                try {
                                                    return MyTweet.downloadImage(mediaEntity[0].getMediaURL(),realSize.y/margin);
                                                } catch (Exception e) {
                                                    return null;
                                                }
                                            }
                                            @Override
                                            protected void onPostExecute(Bitmap bmp){
                                                if(bmp!=null){
                                                    iv.setImageBitmap(bmp);
                                                }
                                            }
                                        };task2.execute(mediae);
                                        i++;
                                    }

                                }
                            }
                            else{
                                constraintLayout.setBackgroundColor(Color.argb(255,0,153,204));
                                MyTweet mt=new MyTweet(getApplicationContext());
                                mt.realwidth=realSize.x;
                                mt.realheight=realSize.y;
                                mt.asyncSetTweet_ConstraintLayout(getApplicationContext(),list,constraintLayout,true);
                                /*AsyncTask<MyTweet,Void,MyTweet> task=new AsyncTask<MyTweet, Void, MyTweet>() {
                                    @Override
                                    protected MyTweet doInBackground(MyTweet... myTweet) {
                                        myTweet[0].setTweet(getApplicationContext(),result,true);
                                        return myTweet[0];
                                    }
                                    @Override
                                    protected  void onPostExecute(MyTweet myTweet){
                                        myTweet.setToConstraintLayout(constraintLayout);
                                    }
                                };
                                task.execute(mt);*/
                            }

                            showToast(String.valueOf(result.size()));
                        }
                    }
                };task.execute(str);
            }
        });
        searchQuery.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN&&!searchQuery.getText().toString().equals("検索"))
                searchQuery.setText("");
                return false;
            }
        });
        atName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN&&!atName.getText().toString().equals("\"@Name\""))
                    atName.setText("");
                return false;
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.app_bar_search){
            finish();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //トーストを表示するメソッド
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
