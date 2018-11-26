package org.example.moviereview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter{
    private  Context mContext;
    private ArrayList<String> array;
    private int width;

    public ImageAdapter(Context c, ArrayList<String> paths, int x){
        mContext = c;
        array = paths;
        width = x;

    }
    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    //for each element of the array in the adapter will call getView once, with different position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null){
            imageView = new ImageView(mContext);
        }else{
            imageView= (ImageView)convertView;
        }
        //while the poster is loading this placeholder poster will show first
        Drawable d = resizeDrawable(mContext.getResources().getDrawable(R.drawable.placeholder));
        //using picasso to load the image with the path into the image view
        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185" + array.get(position)).
                resize(width,(int)(width*1.5)).placeholder(d).into(imageView);
        return imageView;
    }
    private Drawable resizeDrawable(Drawable image){
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b,width,(int)(width*1.5),false);
        return new BitmapDrawable(mContext.getResources(),bitmapResized);
    }
}
