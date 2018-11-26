package org.example.moviereview;


import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;


//in Global class the action will apply to entire application.
public class Global extends Application{
    @Override
    public void onCreate()
    {
        super.onCreate();

        //picasso will save all movie images in the disk cache, picasso automatically load from the cache if cant load from online
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        Picasso.setSingletonInstance(built);


    }
}
