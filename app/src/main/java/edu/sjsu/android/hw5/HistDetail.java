package edu.sjsu.android.hw5;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HistDetail extends Activity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button stockDetail, goHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d("Response:", "> Made it here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_stock_history);
        stockDetail = findViewById(R.id.toDetail);
        goHome = findViewById(R.id.toMain);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        Bundle extra = getIntent().getExtras();
        final String details = extra.getString("input");
        final String input1 = extra.getString("input1");
        final String histD = extra.getString("histD");

        //Log.d("ResponseC:", "> " + details);

        stockDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistDetail.this, StockDetail.class);
                intent.putExtra("input", details);
                intent.putExtra("input1", input1);
                intent.putExtra("histD", histD);
                startActivity(intent);
            }
        });
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( HistDetail.this, MainActivity.class);
                startActivity(intent);
            }
        });

        final List<String> input = new ArrayList<>();
        final List<String> header = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(histD);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Iterator<String> keys = jsonObject.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                            header.add(key);
                            input.add(jsonObject.getString(key));
                    }
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(input, header);
        recyclerView.setAdapter(mAdapter);

    }
}