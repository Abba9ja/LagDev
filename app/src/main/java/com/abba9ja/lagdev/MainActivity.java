package com.abba9ja.lagdev;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Serializable {

    //Declaring
    public static final String USER_DETAIL_KEY = "user";

    TextView count, pageNo, apiLimitError;
    ImageButton prev, next;
    JSONArray items;
    int page = 0;
    ListView userListView;
    String total_count, searchText, incomplete_results, searchTextss;
    private String login, htmlUrl, avatar;
    UserAdapter userAdapter;

    //Setting up a model methods for the User
    public String getLogin() {
        return login;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getAvatar() {
        return avatar;
    }

    public static MainActivity fromJson(JSONObject jsonObject){
        MainActivity mainActivity = new MainActivity();
        try {
            //Deserialize json into object fields
            //Check if a cover edition is available
            mainActivity.login = jsonObject.getString("login");
            mainActivity.htmlUrl = jsonObject.getString("html_url");
            mainActivity.avatar = jsonObject.getString("avatar_url");
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
        //return new object
        return  mainActivity;
    }

    //Decodes array of book json results into business model objects
    public static ArrayList<MainActivity> fromJson(JSONArray jsonArray){
        ArrayList<MainActivity> mainActivitiess = new ArrayList<MainActivity>(jsonArray.length());
        //process each result in json array, decode and convert to business
        //object
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject userJson = null;
            try {
                userJson = jsonArray.getJSONObject(i);
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
            MainActivity mainActivity = MainActivity.fromJson(userJson);
            if(mainActivity != null){
                mainActivitiess.add(mainActivity);
            }
        }
        return  mainActivitiess;
    }
    //End of th User Model

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        count = (TextView) findViewById(R.id.count);
        pageNo = (TextView) findViewById(R.id.page);
        prev = (ImageButton) findViewById(R.id.prev);
        next = (ImageButton) findViewById(R.id.next);
        userListView = (ListView) findViewById(R.id.listView);
        apiLimitError = (TextView) findViewById(R.id.apiLimitError);

        //check if Internet is available
        if(!isInternetConnected(getBaseContext())){
            Toast.makeText(MainActivity.this, "Hmm...you're not connected to the internet",Toast.LENGTH_LONG).show();
            return;
        }
        //the Search query or text
        searchText =  "%20language:java%20location:lagos";

        new Atask().execute(searchText + "&page=" + String.valueOf(page + 1));

        //setting the previous Image Button on OnClickListener
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page -= 1;
                new Atask().execute(searchText + "&page=" + String.valueOf(page + 1));
            }
        });
        //setting the previous Image Button on OnClickListener
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page += 1;
                new Atask().execute(searchText + "&page=" + String.valueOf(page + 1));
            }
        });

    }

    //Method for Check if user using the application is conneted to the internet
    public static boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    //The Atask Class that extends AsyncTask
    class Atask extends AsyncTask<String, Void, Void> {
        private ProgressDialog pDialog;
        boolean apiLimitExceeded = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Getting Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection;
            URL url;
            InputStream inputStream;
            String response = "";

            try {
                url = new URL("https://api.github.com/search/users?q=" + params[0]);
                Log.e("url valeu", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                //set request type
                urlConnection.setRequestMethod("GET");

                urlConnection.setDoInput(true);
                urlConnection.connect();
                //check for HTTP response
                int httpStatus = urlConnection.getResponseCode();
                Log.e("httpstatus", "The response is: " + httpStatus);

                //if HTTP response is 200 i.e. HTTP_OK read inputstream else read errorstream
                if (httpStatus != HttpURLConnection.HTTP_OK) {
                    inputStream = urlConnection.getErrorStream();
                    Map<String, List<String>> map = urlConnection.getHeaderFields();
                    System.out.println("Printing Response Header...\n");
                    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                        System.out.println(entry.getKey()
                                + " : " + entry.getValue());
                    }
                } else {
                    inputStream = urlConnection.getInputStream();
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                while ((temp = bufferedReader.readLine()) != null) {
                    response += temp;
                }
                Log.e("webapi json object", response);
                if (response.contains("API rate limit exceeded")) {
                    apiLimitExceeded = true;
                } else {
                    //convert data string into JSONObject
                    JSONObject obj = (JSONObject) new JSONTokener(response).nextValue();
                    items = obj.getJSONArray("items");

                    total_count = obj.getString("total_count");
                    incomplete_results = obj.getString("incomplete_results");
                }
                urlConnection.disconnect();
            } catch (MalformedURLException | ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!apiLimitExceeded) {
                apiLimitError.setVisibility(View.INVISIBLE);
                setResultListView();
            } else {
                userListView.setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, new ArrayList<>()));
                apiLimitError.setVisibility(View.VISIBLE);
                count.setText("API rate Limit Error!!Try after some time!");
            }
            pDialog.dismiss();
        }
    }

    //method to set the listView Results
    private void setResultListView() {
        //set page no. on the layout
        pageNo.setText("Page " + String.valueOf(page + 1));

        //set TotalCount and error if any
        if (total_count.equals("0")) {
            count.setText("No Repository Found! Try Again!");
            count.setTextColor(Color.RED);
            prev.setEnabled(false);
            next.setEnabled(false);
            return;
        }
        if (incomplete_results.equals("true")) {
            count.setText("Total Count:" + String.valueOf(total_count) + "(NetworkError:Incomplete Result!)");
            count.setTextColor(Color.RED);
        } else {
            count.setText("We’ve found:" + String.valueOf(total_count)+ " Users");
            count.setTextColor(Color.WHITE);
        }

        //parse total page in search result
        int tpcount = Integer.parseInt(total_count);
        int totalpage;
        if (tpcount % 5 == 0) {
            totalpage = tpcount / 5 - 1;
        } else {
            totalpage = tpcount / 5;
        }

        Log.e("total page, page", String.valueOf(totalpage) + ", " + String.valueOf(page));

        //condition to enbable and disable nextpage button and prev page button
        if (page == 0) {
            prev.setEnabled(false);
        } else {
            prev.setEnabled(true);
        }

        if (page == totalpage) {
            next.setEnabled(false);
        } else {
            next.setEnabled(true);
        }


        //finally set listview adaptor

        MainActivity mainActivity = new MainActivity();
        ArrayList <MainActivity> adapterList = new ArrayList<MainActivity>();
        userAdapter = new UserAdapter(this, adapterList);

        //List<List<String>> adapterList = new ArrayList<>();
        if (items.length() == 0) {
            return;
        }
        Log.e("some more data", "item.length" + String.valueOf(items.length()));

            final ArrayList<MainActivity> users = MainActivity.fromJson(items);

                userAdapter.clear();
                //Load model objects into the adapter
                for(MainActivity user: users){
                    userAdapter.add(user); //add book through the adpter;
                }
            userAdapter.notifyDataSetChanged();
            userListView.setAdapter(userAdapter);
            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Launch the detail view passing user as an extra
                    Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
                    intent.putExtra(USER_DETAIL_KEY, userAdapter.getItem(position));
                    startActivity(intent);
                }
            });

    }


    //on KeyDown tto Exist application
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            exitAlert();

        }
        return super.onKeyDown(keyCode, event);
    }

    //the exitAlert() method call in onKeyDown
    public void exitAlert() {
        new AlertDialog.Builder(this).setTitle("Closing App").setMessage("Do you really want to exit application?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //stop activity
                MainActivity.this.finish();


            }
        }).setNegativeButton("No", null).show();

    }


    //The Custom Adapter class UserAdapter to set our customize view
    class UserAdapter extends ArrayAdapter<MainActivity> {
        //View lookup cache
        public class ViewHolder {
            public ImageView ivAvatar;
            public TextView tvLogin;
            public TextView tvHtmlUrl;
        }

        public UserAdapter(Context context, ArrayList<MainActivity> aUsers) {
            super(context, 0, aUsers);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //return super.getView(position, convertView, parent);
            final MainActivity userActivity = getItem(position);

            //Check if an existing view is being resused, otherwise inflate the view
            ViewHolder viewHolder; //view lookup cache stored in tag
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_user, parent, false);
                viewHolder.ivAvatar = (ImageView) convertView.findViewById(R.id.ivAvatar);
                viewHolder.tvLogin = (TextView) convertView.findViewById(R.id.tvLogin);
                viewHolder.tvHtmlUrl = (TextView) convertView.findViewById(R.id.tvHtmlUrl);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //Populate the data into the template view usnig the data onject
            viewHolder.tvLogin.setText(userActivity.getLogin());
            viewHolder.tvHtmlUrl.setText(userActivity.getHtmlUrl());
            Picasso.with(getContext()).load(Uri.parse(userActivity.getAvatar())).into(viewHolder.ivAvatar);
            //Return the completed view to render on screen
            return convertView;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTextss = query+"%20language:java%20location:lagos";
                new Atask().execute(searchTextss + "&page=" + String.valueOf(page + 1));

                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchItem.collapseActionView();
                //set activity title to search query
                MainActivity.this.setTitle(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        if(id == R.id.aboutUs){
            Intent intent = new Intent(MainActivity.this, About.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
