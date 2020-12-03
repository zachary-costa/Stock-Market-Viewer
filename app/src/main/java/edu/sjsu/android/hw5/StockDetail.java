package edu.sjsu.android.hw5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StockDetail extends Activity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button histData, goHome;
    private ImageView star;
    private SharedPreferences favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d("Response:", "> Made it here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_stock_detail);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        histData = findViewById(R.id.toHistory);
        recyclerView.setHasFixedSize(true);
        goHome = findViewById(R.id.toMain);
        star = findViewById(R.id.favorite);
        favorites = getApplicationContext().getSharedPreferences("Favorites", Context.MODE_PRIVATE);

        Bundle extra = getIntent().getExtras();
        final String details = extra.getString("input");
        final String input1 = extra.getString("input1");
        final String histD = extra.getString("histD");

        //Log.d("ResponseC:", "> " + details);

        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( StockDetail.this, MainActivity.class);
                startActivity(intent);
            }
        });

        histData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StockDetail.this, HistDetail.class);
                intent.putExtra("input", details);
                intent.putExtra("input1", input1);
                intent.putExtra("histD", histD);
                startActivity(intent);
            }
        });

        final List<String> input = new ArrayList<>();
        final List<String> header = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(details);
            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                //Log.d("Response:", "> iterating " + key);
                header.add(key);
                input.add(jsonObject.getString(key));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // use a linear layout manager
        //Log.d("ResponseD:", "> " + input);
        int indexOfitem = header.indexOf("name");
        String item = input.get(indexOfitem);
        input.remove(indexOfitem);
        input.add(0, item );
        String item2 = header.get(indexOfitem);
        header.remove(indexOfitem);
        header.add(0, item2 );

        indexOfitem = header.indexOf("ticker");
        item = input.get(indexOfitem);
        input.remove(item);
        input.add(1, item );
        item2 = header.get(indexOfitem);
        header.remove(indexOfitem);
        header.add(1, item2 );

        indexOfitem = header.indexOf("description");
        item = input.get(indexOfitem);
        input.remove(indexOfitem);
        input.add(2, item );
        item2 = header.get(indexOfitem);
        header.remove(indexOfitem);
        header.add(2, item2 );

        indexOfitem = header.indexOf("startDate");
        item = input.get(indexOfitem);
        input.remove(indexOfitem);
        input.add(3, item );
        item2 = header.get(indexOfitem);
        header.remove(indexOfitem);
        header.add(3, item2 );

        try {
            JSONArray jsonArray = new JSONArray(input1);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                if (key.equals("last")) {
                    //Log.d("Response:", "> iterating " + key);
                    header.add(2, key);
                    input.add(2, jsonObject.getString(key));
                }
                else if (key.equals("ticker"))
                {

                }
                else
                {
                    //Log.d("Response:", "> iterating " + key);
                    String value = jsonObject.getString(key);
                    if (value.equals("null"))
                    {
                        header.add(key);
                        input.add("-");
                    }
                    else {
                        header.add(key);
                        input.add(jsonObject.getString(key));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean cF = false;
        Map<String, ?> favMap = favorites.getAll();
        for (Map.Entry<String, ?>entry:favMap.entrySet())
        {
            if (entry.getKey().equalsIgnoreCase(input.get(1)))
            {
                cF = true;
            }
        }
        if (cF)
        {
            star.setImageResource(R.drawable.ic_baseline_star_24);
        }

        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isin = false;
                Map<String, ?> favMap = favorites.getAll();
                for (Map.Entry<String, ?>entry:favMap.entrySet())
                {
                    if (entry.getKey().equalsIgnoreCase(input.get(1)))
                    {
                        isin = true;
                    }
                }
                if (isin)
                {
                    favorites.edit().remove(input.get(1)).commit();
                    star.setImageResource(R.drawable.ic_baseline_star_border_24);
                }
                else
                {
                    String info = input.get(header.indexOf("name")) + " splithere " + input.get(header.indexOf("ticker")) + " splithere " + input.get(header.indexOf("last")) + " splithere " + input.get(header.indexOf("prevClose"));
                    favorites.edit().putString(input.get(1), info).commit();
                    star.setImageResource(R.drawable.ic_baseline_star_24);
                }
            }
        });

        //Log.d("ResponseD:", "> " + input);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(input, header);
        recyclerView.setAdapter(mAdapter);

    }
}
