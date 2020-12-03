package edu.sjsu.android.hw5;


import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends Activity {

    public AutoCompleteTextView autoCompleteTextView;
    private TextView clear, getQuote;
    private ArrayList<String> autoCompleteSuggestions;
    private String[] autoCompleteSuggestions2 = {"Test", "Test1", "Test2"};

    private ImageView refresh;
    private RecyclerView favoritesView;
    private FavoritesAdapter favAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPreferences favorites;
    private Switch autoRefresh;
    private ProgressBar progress;
    private ArrayList<String> favoritesTicker = new ArrayList<>();
    private ArrayList<String> favoritesName = new ArrayList<>();
    private ArrayList<String> favoritesPrice = new ArrayList<>();
    private ArrayList<String> favoritesChange = new ArrayList<>();
    private final static int INTERVAL = 10000;
    private Handler handler = new Handler();
    private Runnable runnable = null;
    boolean isChecked;
    public ArrayAdapter<String> stringArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autoCompleteSuggestions = new ArrayList<>();
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        clear = findViewById(R.id.clearButton);
        getQuote = findViewById(R.id.quoteButton);
        autoRefresh = findViewById(R.id.autoRefreshSwitch);
        refresh = findViewById(R.id.manualRefresh);
        progress = findViewById(R.id.myBarCir);
        stringArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, autoCompleteSuggestions2);
        autoCompleteTextView.setAdapter(stringArrayAdapter);

        refresh.setVisibility(View.VISIBLE);
        progress.getIndeterminateDrawable();
        progress.setVisibility(View.GONE);

        favoritesView = (RecyclerView) findViewById(R.id.favorite_recycler_view);
        favoritesView.setHasFixedSize(true);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                query = query.replace(" ", "+").toLowerCase();
                if (query.length() >= 3)
                {
                    new GetSuggestions(query).execute();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = autoCompleteTextView.getText().toString();
                progress.setVisibility(View.VISIBLE);
                if (input.trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please enter a Stock Name/Symbol", Toast.LENGTH_LONG).show();
                    progress.setVisibility(View.GONE);

                } else {
                    new GetDetails(input).execute();
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoCompleteTextView.setText("");
            }
        });



        ArrayList<String> favoritesList = new ArrayList<>();
        favorites = getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        Map<String, ?> favMap = favorites.getAll();
        for (Map.Entry<String, ?>entry:favMap.entrySet())
        {
            favoritesList.add(entry.getValue().toString());
        }

        //Log.d("Response:", "favorites list =  " + favoritesList);
        for (String s : favoritesList) {
            favoritesName.add(s.split(" splithere ")[0].trim());
            favoritesTicker.add(s.split(" splithere ")[1].trim());
            favoritesPrice.add(s.split(" splithere ")[2].trim());
            double t = Double.parseDouble(s.split(" splithere ")[2].trim());
            //Log.d("Response:", "t =  " + t);

            double p = Double.parseDouble(s.split(" splithere ")[3].trim());
            //Log.d("Response:", "p =  " + p);

            double perChange = ((t - p) / t)*100;
            favoritesChange.add(String.format("%.2f",perChange));
        }
        layoutManager = new LinearLayoutManager(this);
        favoritesView.setLayoutManager(layoutManager);
        favAdapter = new FavoritesAdapter(favoritesName, favoritesTicker, favoritesPrice, favoritesChange);
        favoritesView.setAdapter(favAdapter);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);
                favoritesView.setVisibility(View.GONE);
                new RefreshFavorites(favoritesTicker).execute();
            }
        });

        autoRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    isChecked = true;
                    runnable = new Runnable() {
                        public void run() {
                            //Log.d("Response:", "running through ischecked =  " + isChecked);
                            handler.postDelayed(this, INTERVAL);
                            progress.setVisibility(View.VISIBLE);
                            favoritesView.setVisibility(View.GONE);
                            new RefreshFavorites(favoritesTicker).execute();
                        }
                    };
                    autoRefreshInterval();
                }
                else
                {
                    isChecked = false;
                    autoRefreshInterval();
                }
            }
        });
    }
    private void autoRefreshInterval() {
        if (isChecked == true) {
            handler.postDelayed(runnable, INTERVAL);
        }
        else
        {
            //Log.d("Response:", "removedcallbacks > ");
            handler.removeCallbacks(runnable);
        }
    }

    private class RefreshFavorites extends AsyncTask<String, Void, String> {
        private ArrayList<String> input;
        private ArrayList<String> output = new ArrayList<>();
        private ArrayList<String> outputC = new ArrayList<>();
        private ArrayList<String> outputPrev = new ArrayList<>();

        RefreshFavorites(ArrayList<String> i) {
            input = i;
        }

        @Override
        protected String doInBackground(String... strings) {
            for (String s : input) {
                HttpURLConnection urlConnection = null;

                URL url = null;
                try {
                    url = new URL("https://api.tiingo.com/iex/?tickers=" + s + "&token=5889ac28d065b53d35510353756d0c7ab97e0f52");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setInstanceFollowRedirects(false);
                    urlConnection.connect();
                    InputStream stream = urlConnection.getInputStream();
                    InputStreamReader isReader = new InputStreamReader(stream);
                    BufferedReader reader = new BufferedReader(isReader);
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    //Log.d("Response:", "> Ticker Value = " + s);
                    //Log.d("Response:", "> resulting value = " + String.valueOf(sb));
                    JSONArray jsonArray = new JSONArray(String.valueOf(sb));
                    output.add(jsonArray.getJSONObject(0).getString("last"));
                    double t = Double.parseDouble(jsonArray.getJSONObject(0).getString("last"));
                    double p = Double.parseDouble(jsonArray.getJSONObject(0).getString("prevClose"));
                    double perChange = ((t - p) / t)*100;
                    outputC.add(String.format("%.2f",perChange));
                    outputPrev.add(jsonArray.getJSONObject(0).getString("prevClose"));
                } catch (MalformedURLException | ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            //output = new ArrayList<>();
            //output.add("100.00");
            //output.add("10.00");
            favoritesPrice = output;
            favoritesChange = outputC;
            //Log.d("Response:", "refreshFavorites Call for favoritechanges > " + favoritesChange);
            return null;
        }

        protected void onPostExecute(String results) {
            //Log.d("ResponseD:", "refreshedfavsZY > " + favoritesPrice);
            favAdapter.update(favoritesName, favoritesTicker, favoritesPrice, favoritesChange);
            favAdapter.notifyDataSetChanged();
            favoritesView.invalidate();
            for (String s : favoritesTicker)
            {
                int i = favoritesTicker.indexOf(s);
                String info = favoritesName.get(i) + " splithere " + favoritesTicker.get(i) + " splithere " + favoritesPrice.get(i) + " splithere " + outputPrev.get(i);
                favorites.edit().remove(s).commit();
                favorites.edit().putString(s, info).commit();
            }
            progress.setVisibility(View.GONE);
            favoritesView.setVisibility(View.VISIBLE);
        }
    }
    private class GetDetails extends AsyncTask<String, Void, String> {
        private String input = "";
        GetDetails(String i) {
            input = i;
        }

        @Override
        protected String doInBackground(String... objects) {
            String tickerValue = input.split("-")[0].trim();
            //Log.d("Response:", "> Ticker Value = " + tickerValue);
            String results = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("https://api.tiingo.com/tiingo/daily/" + tickerValue + "?token=5889ac28d065b53d35510353756d0c7ab97e0f52");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.connect();
                InputStream stream = urlConnection.getInputStream();
                InputStreamReader isReader = new InputStreamReader(stream);
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                results = sb.toString();
                //Log.d("Response:", ">  converted to string = " + results);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Invalid Symbol", Toast.LENGTH_LONG).show();
                    }
                });
            }

            urlConnection = null;

            try {
                URL url = new URL("https://api.tiingo.com/iex/?tickers=" + tickerValue + "&token=5889ac28d065b53d35510353756d0c7ab97e0f52");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.connect();
                InputStream stream = urlConnection.getInputStream();
                InputStreamReader isReader = new InputStreamReader(stream);
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                results = results + " splithere " + sb.toString();
                //Log.d("Response:", ">  converted to string = " + results);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Invalid Symbol", Toast.LENGTH_LONG).show();
                    }
                });
            }


            urlConnection = null;

            try {
                URL url = new URL("https://api.tiingo.com/tiingo/daily/" + tickerValue + "/prices?startDate=2010-01-01&resampleFreq=monthly&token=5889ac28d065b53d35510353756d0c7ab97e0f52");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.connect();
                InputStream stream = urlConnection.getInputStream();
                InputStreamReader isReader = new InputStreamReader(stream);
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                results = results + " splithere " + sb.toString();
                //Log.d("Response:", ">  converted to string = " + results);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Invalid Symbol", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return results;
        }

        protected void onPostExecute(String results) {
            //Log.d("Response:", "> onPostExecute GetDetails " + results);
            try {
                String results0 = results.split(" splithere ")[0].trim();
                String results1 = results.split(" splithere ")[1].trim();
                String results2 = results.split(" splithere ")[2].trim();
                Intent intent = new Intent(MainActivity.this, StockDetail.class);
                intent.putExtra("input", results0);
                intent.putExtra("input1", results1);
                intent.putExtra("histD", results2);
                startActivity(intent);
            }
            catch (IndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }
            progress.setVisibility(View.GONE);
        }
    }

    private class GetSuggestions extends AsyncTask<String, Void, String> {

        private String query = "";
        GetSuggestions(String q) {
            query = q;
        }

        @Override
        protected String doInBackground(String... objects) {
            String results = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("https://api.tiingo.com/tiingo/utilities/search?query=" + query + "&token=5889ac28d065b53d35510353756d0c7ab97e0f52");
                //Log.d("Response:", "> Query = " + query);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.connect();
                InputStream stream = urlConnection.getInputStream();
                InputStreamReader isReader = new InputStreamReader(stream);
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                results = sb.toString();
                //Log.d("Response:", "> " + results);
            }
             catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return results.toString();
        }

        protected void onPostExecute(String results) {
            autoCompleteSuggestions.clear();
                try {
                    //Log.d("ResponseX:", "> " + results);
                    JSONArray jsonArray = new JSONArray(results);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        autoCompleteSuggestions.add(jsonArray.getJSONObject(i).getString("ticker") + " - " + jsonArray.getJSONObject(i).getString("name"));
                    }
                    stringArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, autoCompleteSuggestions);
                    autoCompleteTextView.setAdapter(stringArrayAdapter);
                    stringArrayAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }
}
