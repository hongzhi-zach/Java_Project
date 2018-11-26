package org.example.moviereview;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.PreferenceChangeListener;


public class MoviesFragment extends Fragment {
    static GridView gridView;
    static int width;
    static ArrayList<String> posters;
    static boolean sortByPop = true;
    static String API_KEY = "6b3f7d15b2799e917bee6d9a3bcc3431";

    static PreferenceChangeListener listener;
    static SharedPreferences prefs;
    static boolean sortByFavorites;
    //arraylist for favorited posters
    static ArrayList<String> postersF;
    static ArrayList<String> titlesF;
    static ArrayList<String> datesF;
    static ArrayList<String> ratingsF;
    static ArrayList<String> overviewsF;
    static ArrayList<String> youtubesF;
    static ArrayList<String> youtubes2F;
    static ArrayList<ArrayList<String>> commentsF;


    static ArrayList<String> overview;
    static ArrayList<String> titles;
    static ArrayList<String> dates;
    static ArrayList<String> ratings;
    static ArrayList<String> youtubes;
    static ArrayList<String> youtubes2;
    static ArrayList<String> ids;
    static ArrayList<Boolean> favorited;
    static ArrayList<ArrayList<String>> comments;


    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //if the device is tablet we want the width of each poster to be 1/6 of the screen, if on the phone we want 1/3 of the screen
        if (MainActivity.TABLET) {
            width = size.x / 6;
        } else width = size.x / 3;
        if (getActivity() != null) {
            ArrayList<String> array = new ArrayList<String>();
            ImageAdapter adapter = new ImageAdapter(getActivity(), array, width);
            gridView = (GridView) rootView.findViewById(R.id.gridview);

            gridView.setColumnWidth(width);
            gridView.setAdapter(adapter);
        }
        //this method listens for pressed on gridview items
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                if (!sortByFavorites) {
                    favorited = bindFavoritesToMovies();

                    Intent intent = new Intent(getActivity(), DetailActivity.class).
                            putExtra("overview", overview.get(position)).
                            putExtra("poster", posters.get(position)).
                            putExtra("title", titles.get(position)).
                            putExtra("date", dates.get(position)).
                            putExtra("rating", ratings.get(position)).
                            putExtra("youtube", youtubes.get(position)).
                            putExtra("youtube2", youtubes2.get(position)).
                            putExtra("comments", comments.get(position)).
                            putExtra("favorite", favorited.get(position));
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(getActivity(), DetailActivity.class).
                            putExtra("overview", overviewsF.get(position)).
                            putExtra("poster", postersF.get(position)).
                            putExtra("title", titlesF.get(position)).
                            putExtra("date", datesF.get(position)).
                            putExtra("rating", ratingsF.get(position)).
                            putExtra("youtube", youtubesF.get(position)).
                            putExtra("youtube2", youtubes2F.get(position)).
                            putExtra("comments", commentsF.get(position)).
                            putExtra("favorite", favorited.get(position));
                    startActivity(intent);
                }
            }
            }
        );


        return rootView;
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        //when the preference is changed, the grid view will be automatically set to null and onStart() will be called
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            gridView.setAdapter(null);
            onStart();
        }
    }


    public ArrayList<Boolean> bindFavoritesToMovies()
    {
        //return the array of booleans for the true or false for each movie whether they are favorited or not
        ArrayList<Boolean> result = new ArrayList<>();
        for(int i=0;i<titles.size();i++)
        {
            result.add(false);
        }
        for(String favoritedTitles: titlesF)
        {
            for(int x = 0; x<titles.size();x++)
            {
                if(favoritedTitles.equals(titles.get(x)))
                {
                    result.set(x,true);
                }
            }
        }
        return result;
    }
    @Override
    public void onStart() {
        super.onStart();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        listener = new PreferenceChangeListener();
        prefs.registerOnSharedPreferenceChangeListener(listener);

        //preference name is sortby, and the values are popularity, rating
        if (prefs.getString("sortby", "popularity").equals("popularity")) {
            getActivity().setTitle("Most Popular Movies");
            sortByPop = true;
            sortByFavorites = false;
        } else if (prefs.getString("sortby", "popularity").equals("rating")) {
            getActivity().setTitle("Highest Rated Movies");
            sortByPop = false;
            sortByFavorites = false;
        } else if (prefs.getString("sortby", "popularity").equals("favorites")) {
            getActivity().setTitle("Favorites Movies");
            sortByPop = false;
            sortByFavorites = true;
        }

        TextView textView = new TextView(getActivity());
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.linearlayout);
        loadFavoritesData();

        if (sortByFavorites ) {
            if (postersF.size() == 0) {
                textView.setText("you have no favorite movies.");
                if (layout.getChildCount() == 1)
                    layout.addView(textView);
                gridView.setVisibility(GridView.GONE);
            } else {
                gridView.setVisibility(GridView.VISIBLE);
                layout.removeView(textView);
            }
            if (postersF != null && getActivity() != null) {
                ImageAdapter adapter = new ImageAdapter(getActivity(), postersF, width);
                gridView.setAdapter(adapter);
            }
        } else {
            gridView.setVisibility(GridView.VISIBLE);
            layout.removeView(textView);


            if (isNetworkAvailable()) {

                //this loading process has to be done in the background thread since user want to interact with app on the front
                new ImageLoadTask().execute();
            }
            //if there is no internet connection then give a textview hint the error and hide gridview
            else {
                TextView textview1 = new TextView(getActivity());
                LinearLayout layout1 = (LinearLayout) getActivity().findViewById(R.id.linearlayout);
                textview1.setText("No network Connection");
                if (layout1.getChildCount() == 1) {
                    layout1.addView(textview1);
                }
                gridView.setVisibility(GridView.GONE);
            }
        }
    }

    public void loadFavoritesData(){
        String URL = "content://org.example.provider.Movies/movies";
        Uri movies = Uri.parse(URL);
        Cursor c = getActivity().getContentResolver().query(movies, null, null, null, "title");
        postersF = new ArrayList<String>();
        titlesF = new ArrayList<String>();
        datesF = new ArrayList<String>();
        overviewsF = new ArrayList<String>();
        favorited = new ArrayList<Boolean>();
        ratingsF = new ArrayList<String>();
        youtubesF = new ArrayList<String>();
        youtubes2F = new ArrayList<String>();
        commentsF = new ArrayList<ArrayList<String>>();
        if(c==null) return;
        while(c.moveToNext())
        {
            postersF.add(c.getString(c.getColumnIndex(MovieProvider.NAME)));
            commentsF.add(convertStringToArrayList(c.getString(c.getColumnIndex(MovieProvider.REVIEW))));
            titlesF.add(c.getString(c.getColumnIndex(MovieProvider.TITLE)));
            overviewsF.add(c.getString(c.getColumnIndex(MovieProvider.OVERVIEW)));
            youtubesF.add(c.getString(c.getColumnIndex(MovieProvider.YOUTUBE1)));
            youtubes2F.add(c.getString(c.getColumnIndex(MovieProvider.YOUTUBE2)));
            datesF.add(c.getString(c.getColumnIndex(MovieProvider.DATE)));
            ratingsF.add(c.getString(c.getColumnIndex(MovieProvider.RATING)));
            favorited.add(true);

        }


    }

    public ArrayList<String> convertStringToArrayList(String s)
    {
        //reformat the string to array of strings, the "divider123" is a mark made by us to split the comments
        ArrayList<String> result = new ArrayList<>(Arrays.asList(s.split("divider123")));
        return result;
    }

    //check if there is internet connection from Manifest file
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, ArrayList<String>> {

        //this method returns the a list of each poster's path
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            while (true) {
                try {
                    posters = new ArrayList(Arrays.asList(getPathsFromAPI(sortByPop)));
                    return posters;
                } catch (Exception e) {
                    continue;

                }
            }
        }

        //this method place the posters that returned from above method
        @Override
        protected void onPostExecute(ArrayList<String> result) {

            if (result != null && getActivity() != null) {
                ImageAdapter adapter = new ImageAdapter(getActivity(), result, width);
                gridView.setAdapter(adapter);
            }
        }

        public String[] getPathsFromAPI(boolean sortbypop) {

            while (true) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String JSONResult;
                try {
                    String urlString = null;
                    //here we create the URLs from API database
                    if (sortbypop) {
                        urlString = "https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=" + API_KEY;
                    }
                    //if it's not sort by popular then return the movies with at least 500 votes from movie API
                    else {
                        urlString = "https://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&vote_count.gte=500&api_key=" + API_KEY;
                    }
                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    //read the input stream into a string
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    //save the movie infos in the json object
                    JSONResult = buffer.toString();

                    //then try to get paths from json walltext
                    try {
                        overview = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult, "overview")));
                        titles = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult, "original_title")));
                        ratings = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult, "vote_average")));
                        dates = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult, "release_date")));
                        //ids are used to get youtube links and comments from youtube
                        ids = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult, "id")));
                        //putting loop here because the call for youtube links are inconsistent, so keep calling until not getting null or getting less than 2 video links
                        while (true) {
                            youtubes = new ArrayList<String>(Arrays.asList(getYoutubesFromIds(ids, 1)));
                            youtubes2 = new ArrayList<String>(Arrays.asList(getYoutubesFromIds(ids, 1)));
                            int nullCount = 0;
                            for (int i = 0; i < youtubes.size(); i++) {
                                if (youtubes.get(i) == null) {
                                    nullCount++;
                                    youtubes.set(i, "no video found");
                                }
                            }

                            for (int i = 0; i < youtubes2.size(); i++) {
                                if (youtubes2.get(i) == null) {
                                    nullCount++;
                                    youtubes2.set(i, "no video found");
                                }
                            }
                            if (nullCount > 2) {
                                continue;
                            }
                            break;
                        }

                        comments = getReviewsFromIds(ids);
                        return getPathsFromJson(JSONResult);

                    } catch (JSONException e) {
                        return null;
                    }
                }
                //if exceptions occurs above then repeat the infinite loop
                catch (Exception e) {
                    continue;
                }
                //disconnect the urlconnection and close the reader afterwards
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {

                        }
                    }
                }
            }
            /*  this is a method in testing phase that we acquired a sample poster for the Movie array
            String[] array = new String[15];
            for(int i = 0; i<array.length;i++ ){
                array[i] = "/kqjL17yufvn9OVLyXYpvtyrFfak.jpg";
            }
            return array;
            */
        }

        //method that extract movie poster path from json object, then return as an path array
        public String[] getPathsFromJson(String JSONStringParam) throws JSONException {
            //first get the json object from above method
            JSONObject JSONString = new JSONObject(JSONStringParam);
            //then create a movie array object that represent the 'result' array that holds array of all movie objects
            JSONArray moviesArray = JSONString.getJSONArray("results");
            String[] result = new String[moviesArray.length()];
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movie = moviesArray.getJSONObject(i);
                String moviePath = movie.getString("poster_path");
                result[i] = moviePath;
            }
            return result;
        }

        //similar to get path method here we get overviews and stuff from json object
        public String[] getStringsFromJSON(String JSONStringParam, String param) throws JSONException {
            JSONObject JSONString = new JSONObject(JSONStringParam);
            JSONArray moviesArray = JSONString.getJSONArray("results");
            String[] result = new String[moviesArray.length()];
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movie = moviesArray.getJSONObject(i);
                if (param.equals("vote_average")) {
                    Double number = movie.getDouble("vote_average");
                    String rating = Double.toString(number) + "/10";
                    result[i] = rating;
                } else {
                    String data = movie.getString(param);
                    result[i] = data;
                }
            }
            return result;
        }

        public ArrayList<ArrayList<String>> getReviewsFromIds(ArrayList<String> ids)
        {
            outerloop:
            while(true)
            {
                ArrayList<ArrayList<String>> results = new ArrayList<>();
                for (int i = 0; i < ids.size(); i++) {
                    HttpURLConnection urlConnection = null;
                    BufferedReader reader = null;
                    String JSONResult;
                    try {
                        String urlString = null;
                        urlString = "https://api.themoviedb.org/3/movie/" + ids.get(i) + "/reviews?api_key=" + API_KEY;


                        URL url = new URL(urlString);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        //read the input stream into a string
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            return null;
                        }
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line + "\n");
                        }
                        if (buffer.length() == 0) {
                            return null;
                        }
                        //save the movie infos in the json object
                        JSONResult = buffer.toString();
                        try {

                            results.add(getCommentsFromJSON(JSONResult));
                        } catch (JSONException E) {

                            return null;
                        }


                    } catch (Exception e) {

                        continue outerloop;
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (final IOException e) {

                            }
                        }
                    }

                }
                return results;
            }
        }

        public ArrayList<String> getCommentsFromJSON(String JSONStringParam) throws JSONException
        {
            JSONObject JSONString = new JSONObject(JSONStringParam);
            JSONArray reviewsArray = JSONString.getJSONArray("results");
            ArrayList<String> results = new ArrayList<>();
            if(reviewsArray.length()==0)
            {
                results.add("No reviews found for this movie.");
                return results;
            }
            for(int i=0; i<reviewsArray.length();i++){
                JSONObject result = reviewsArray.getJSONObject(i);
                results.add(result.getString("content"));
            }
            return results;
        }

        public String[] getYoutubesFromIds(ArrayList<String> ids, int position) {

            String[] results = new String[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String JSONResult;
                try {
                    String urlString = null;
                    urlString = "https://api.themoviedb.org/3/movie/" + ids.get(i) + "/videos?api_key=" + API_KEY;


                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    //read the input stream into a string
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    //save the movie infos in the json object
                    JSONResult = buffer.toString();
                    try {

                        results[i] = getYoutubeFromJSON(JSONResult, position);
                    } catch (JSONException E) {

                        results[i] = "no video found";
                    }


                } catch (Exception e) {

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {

                        }
                    }
                }

            }
            return results;

        }
        public String getYoutubeFromJSON(String JOSNStringParam, int position) throws JSONException
        {
            JSONObject JSONString = new JSONObject(JOSNStringParam);
            JSONArray youtubesArray = JSONString.getJSONArray("results");
            JSONObject youtube;
            String result = "no videos found";
            if(position == 0)
            {
                youtube = youtubesArray.getJSONObject(0);
                result = youtube.getString("key");
            }
            else if(position == 1)
            {
                if(youtubesArray.length()>1)
                {
                    youtube = youtubesArray.getJSONObject(1);
                }
                else{
                    youtube = youtubesArray.getJSONObject(0);
                }
                result = youtube.getString("key");
            }
            return result;
        }
    }
}
