package com.wt.project.twitter;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterFunction {
    Twitter twitter;
    static String filter_safe=" filter:safe";
    static String filter_media=" filter:images";
    long lastID=-1;
    TwitterFunction(Twitter tw){
        twitter=tw;
    }

    List<Status> searchTwittter(String que){
        Query query=new Query(que);
        query.setCount(100);
        if(lastID!=-1){
            //query.setSinceId(lastID);
        }
        QueryResult result = null;
        try {
            result = twitter.search(query);
            //lastID=result.getMaxId();
            return result.getTweets();
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }

    String makeQuery(String name,String text,String filter){
        String str="";
        if(name!=null)str+="@"+name+" ";
        if(text!=null)str+=text;
        if(filter!=null)str+=filter;
        return str;
    }
}
