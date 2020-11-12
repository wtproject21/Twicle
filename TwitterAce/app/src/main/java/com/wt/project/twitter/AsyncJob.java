package com.wt.project.twitter;


import android.os.AsyncTask;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

class AsyncJob extends AsyncTask<Twitter, User,User>  {

    private MainActivity _main;

    public AsyncJob(MainActivity main) {
        super();
        _main = main;
    }


    @Override
    protected User doInBackground(Twitter... value) {
        User user = null;
        try {
            user = value[0].verifyCredentials();
        } catch (TwitterException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return user;
    }

    @Override
    protected void onProgressUpdate(User... values) {
        //

    }

    @Override
    protected void onPostExecute(User result) {
        _main.result_job(result);
    }



}

