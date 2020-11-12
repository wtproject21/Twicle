package com.wt.project.twitter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class IndexActivity extends Activity {
    private OAuthAuthorization twitterOauth;
    private RequestToken requestToken;
    private AccessToken accessToken;
    private SharedPreferences pref;
        /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        pref = getSharedPreferences(TwitterConst.PREF_KEY,MODE_PRIVATE);
        if (pref.getString("status", "").equals("available")) {
            Toast.makeText(IndexActivity.this, "already oauth. ", Toast.LENGTH_LONG).show();
        }
	        else {
            Toast.makeText(IndexActivity.this, "disable oauth. ", Toast.LENGTH_LONG).show();
        }
        Button tweetBotton = findViewById(R.id.tweet);
        tweetBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.getString("status", "").equals("available")) {
                    tweet();
                }
                else {
                    authentication();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            accessToken = twitterOauth.getOAuthAccessToken(requestToken,intent.getExtras().getString(TwitterConst.PARAM_OAUTH_VERIFIER));
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        SharedPreferences pref = getSharedPreferences(TwitterConst.PREF_KEY,MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        editor.putString(TwitterConst.SUB_KEY_OAUTH_TOKEN,accessToken.getToken());
        editor.putString(TwitterConst.SUB_KEY_OAUTH_TOKEN_SECRET,accessToken.getTokenSecret());
        editor.putString("status","available");
        editor.commit();
        Toast.makeText(IndexActivity.this, "OAuth end.", Toast.LENGTH_LONG).show();
        finish();
    }
    private void authentication() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TwitterConst.CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TwitterConst.CONSUMER_SECRET);
        Configuration configuration = builder.build();
        twitterOauth = new OAuthAuthorization(configuration);
        twitterOauth.setOAuthAccessToken(null);
        try {
            requestToken = twitterOauth.getOAuthRequestToken(TwitterConst.CALLBACK_URL);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(IndexActivity.this, LoginTwitterActivity.class);
        intent.putExtra("auth_url", requestToken.getAuthorizationURL());
        IndexActivity.this.startActivityForResult(intent, 1);
    }
    private void tweet(){
        String oauthToken       = pref.getString("oauth_token", "");
        String oauthTokenSecret = pref.getString("oauth_token_secret", "");
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TwitterConst.CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TwitterConst.CONSUMER_SECRET);
        builder.setOAuthAccessToken(oauthToken);
        builder.setOAuthAccessTokenSecret(oauthTokenSecret);
        Configuration config = builder.build();
        Twitter twitter = new TwitterFactory(config).getInstance();
        /*try {
            twitter.updateStatus("※ここに記載した内容がツイートされます。※");
        } catch (TwitterException e) {
            e.printStackTrace();
        }*/
    }
}