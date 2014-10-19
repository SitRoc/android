package com.example.steve11cortism.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainList extends ListActivity {

//    protected String[] mBlogPostTitles = {
//            "Android Beta",
//            "Android 1.0",
//            "Android 1.1",
//            "Cupcake",
//            "Donut",
//            "Eclair",
//            "Froyo",
//            "Gingerbread",
//            "Honeycomb",
//            "Ice Cream Sandwich",
//            "Jelly Bean"
//    };

    //protected String[] mBlogPostTitles;
    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainList.class.getSimpleName();
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;
    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "author";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);

        if(isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            GetBlogPostTask getBlogPostTask = new GetBlogPostTask();
            getBlogPostTask.execute();
        }

//        //Getting an array from the resource file
//        Resources resources = getResources();
//        mBlogPostTitles = resources.getStringArray(R.array.android_names);
//
//        //Adapts the string array mBlogPostTitles to a list view
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mBlogPostTitles);
//        //Setting the adapter in the List adapter
//        setListAdapter(adapter);

        //String message = getString(R.string.no_items);
        //Toast.makeText(this,message,Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            JSONArray jsonPosts = mBlogData.getJSONArray("posts");
            JSONObject jsonPost = jsonPosts.getJSONObject(position);
            String blogUrl = jsonPost.getString("url");
            //Intent intent = new Intent(Intent.ACTION_VIEW);
            Intent intent = new Intent(this, BlogWebViewActivity.class);
            intent.setData(Uri.parse(blogUrl));
            startActivity(intent);
        } catch (JSONException e) {
            logException(e);
        }


    }

    private void logException(Exception e) {
        Log.e(TAG, "Exception caught: ", e);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //We needed to Add a user_permission of type: android.permission.ACCESS_NETWORK_STATE
        boolean isAvailable = false;

        //We are checking if the networkInfo is not null and if networkInfo is connected
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }else{
            Toast.makeText(this, "Network is Unavailable!",Toast.LENGTH_LONG).show();
        }

        return isAvailable;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleBlogResponse() {

        mProgressBar.setVisibility(View.INVISIBLE);

        if(mBlogData == null){
            updateDisplayForError();
        }else{
            try {
                JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                //mBlogPostTitles = new String[jsonPosts.length()];
                ArrayList<HashMap<String, String>> blogPosts = new ArrayList<HashMap<String, String>>();
                for (int i = 0; i<jsonPosts.length(); i++)
                {
                    JSONObject post = jsonPosts.getJSONObject(i);
                    String title = post.getString(KEY_TITLE);
                    title = Html.fromHtml(title).toString();
                    String author = post.getString(KEY_AUTHOR);
                    author = Html.fromHtml(author).toString();
                    //mBlogPostTitles[i] = title;

                    HashMap<String, String> blogPost = new HashMap<String, String>();
                    blogPost.put(KEY_TITLE,title);
                    blogPost.put(KEY_AUTHOR,author);

                    blogPosts.add(blogPost);
                }

//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mBlogPostTitles);
//                setListAdapter(adapter);

                String[] keys = {KEY_TITLE, KEY_AUTHOR};
                int[] ids = {android.R.id.text1, android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2,keys,ids);

                setListAdapter(adapter);

            } catch (JSONException e) {
                Log.e(TAG,"Exception caught!");
            }
        }

    }

    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        //Setting the No items to display! message
        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }

    //Async Class to help us create an async thread
    private class GetBlogPostTask extends AsyncTask<Object, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(Object[] objects) {

            int responseCode = -1;

            JSONObject jsonResponse = null;

            try {
                URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                //We needed to Add a user_permission of type: android.permission.INTERNET

                if(responseCode == HttpURLConnection.HTTP_OK){
                    //Get the JSON files
                    InputStream inputStream = connection.getInputStream(); //Getting Input stream from the connection we just made
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);
                    String responseData = new String(charArray);
                    //Log.v(TAG, responseData);
                    jsonResponse = new JSONObject(responseData);
//                    String status = jsonResponse.getString("status");
//                    Log.v(TAG, status);
//
//                    JSONArray jsonPosts = jsonResponse.getJSONArray("posts");
//
//                    for (int i = 0; i<jsonPosts.length();i++){
//                        JSONObject jsonPost = jsonPosts.getJSONObject(i);
//                        String title = jsonPost.getString("title");
//                        Log.v(TAG,"Post " + i + ": " + title);
//                    }

                }else{
                    Log.i(TAG,"Unsuccseful HTTP Response Code: " + responseCode);
                }


            }catch (MalformedURLException e){
                logException(e);
            }catch (IOException e){
                logException(e);
            }catch (Exception e){
                logException(e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result){
            mBlogData = result;
            handleBlogResponse();
        }

    }


}
