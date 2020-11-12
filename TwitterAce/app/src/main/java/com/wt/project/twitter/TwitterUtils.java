package com.wt.project.twitter;

//https://mltmdkana.hatenablog.com/entry/2016/11/06/143739より

import android.content.Context;
import android.content.SharedPreferences;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterUtils {

    //SharedPrerence用のキー
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";


    //Twitterインスタンスの生成
    public static Twitter getTwitterInstance(Context context){
        //string.xmlで記述した設定の呼び出し
        String consumerKey = context.getString(R.string.twitter_consumer_key);
        String consumerSecret = context.getString(R.string.twitter_consumer_secret);

        //Twitterオブジェクトのインスタンス
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        //トークンの設定
        if(hasAccessToken(context)){
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        return twitter;
    }

    //トークンの格納
    public static void storeAccessToken(Context context, AccessToken accessToken) {

        //トークンの設定
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());

        //トークンの保存
        editor.commit();
    }

    //トークンの格納
    public static void removeAccessToken(Context context) {

        //トークンの設定
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(TOKEN);
        editor.remove(TOKEN_SECRET);

        //トークンの保存
        editor.commit();
    }

    //トークンの読み込み
    public static AccessToken loadAccessToken(Context context) {

        //preferenceからトークンの呼び出し
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        if(token != null && tokenSecret != null){
            return new AccessToken(token, tokenSecret);
        }
        else{
            return null;
        }
    }

    //トークンの有無判定
    public static boolean hasAccessToken(Context context) {
        System.out.println(loadAccessToken(context) != null);
        return  loadAccessToken(context) != null;
    }


}