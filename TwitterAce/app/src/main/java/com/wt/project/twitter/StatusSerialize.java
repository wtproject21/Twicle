package com.wt.project.twitter;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import java.io.Serializable;

import twitter4j.Status;

public class StatusSerialize implements Serializable {
    Status status;
    View[] v={};
    RemoteViews views=null;
    Boolean isWidget=false;
    int ID;
    int widgetID;
    int[] colors={Color.argb(200,100,150,100),Color.argb(200,150,100,100),Color.argb(200,100,100,150),Color.argb(0,0,0,0)};
    int i=0;
    Context context;
    boolean favorite=false;
    boolean retweeted=false;
    void setColor(int num){
        if(v.length!=0)
            v[0].setBackgroundColor(colors[i+num]);
    }
    void setWidgetBackgroundColor(int num){
        if(num==1)favorite=true;
        else if(num==2)retweeted=true;
        else if(num==-1)favorite=false;
        else if(num==-2)retweeted=false;
        if(views!=null){
            int choice=colors[3];
            if(favorite&&retweeted)choice=colors[0];
            else if(favorite)choice=colors[1];
            else if(retweeted)choice=colors[2];
            views.setInt(ID,"setBackgroundColor",choice);
            AppWidgetManager.getInstance(context).updateAppWidget(widgetID,views);
        }
    }
    void setStatus(Status status){
        this.status=status;
        favorite=status.isFavorited();
        retweeted=status.isRetweeted();
    }
    void setWidgetInfo(int widgetID,int itemID){
        this.widgetID=widgetID;
        ID=itemID;
        isWidget=true;
    }
}
