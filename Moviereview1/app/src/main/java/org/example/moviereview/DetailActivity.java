package org.example.moviereview;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if(savedInstanceState== null)
        {
            getSupportFragmentManager().beginTransaction().add(R.id.containerDetail, new DetailActivityFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        //menu_detail has the share function in it
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void favorite(View v)
    {
        Button b = (Button)findViewById(R.id.favorite);
        if(b.getText().equals("FAVORITE"))
        {
            //code to store movie data in database

            b.setText("UNFAVORITE");
            b.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);

            ContentValues values = new ContentValues();
            values.put(MovieProvider.NAME, DetailActivityFragment.poster);
            values.put(MovieProvider.OVERVIEW, DetailActivityFragment.overview);
            values.put(MovieProvider.RATING, DetailActivityFragment.rating);
            values.put(MovieProvider.DATE, DetailActivityFragment.date);
            values.put(MovieProvider.REVIEW, DetailActivityFragment.review);
            values.put(MovieProvider.YOUTUBE1, DetailActivityFragment.youtube);
            values.put(MovieProvider.YOUTUBE2, DetailActivityFragment.youtube2);
            values.put(MovieProvider.TITLE, DetailActivityFragment.title);

            getContentResolver().insert(MovieProvider.CONTENT_URI, values);

        }
        else
        {
            b.setText("FAVORITE");
            b.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            //go through the database and find and delete the matched movie from the favorite list
            getContentResolver().delete(Uri.parse("content://org.example.provider.Movies/movies"),
                    "title=?", new String[]{DetailActivityFragment.title});
        }
    }
    public void trailer1(View v)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com" +
                "/watch?v="+ DetailActivityFragment.youtube));
        startActivity(browserIntent);
    }
    public void trailer2(View v)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com" +
                "/watch?v="+ DetailActivityFragment.youtube2));
        startActivity(browserIntent);
    }
}
