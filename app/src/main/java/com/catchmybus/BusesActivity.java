package com.catchmybus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.adapters.GridBusesAdapter;
import com.catchmybus.settings.AppSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BusesActivity extends AppCompatActivity {

    private String sourceLocationId, destinationLocationId;
    private GridView busesGv;
    private GridBusesAdapter adapter;
    private ArrayList<JSONObject> buses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buses);
        Intent sourceIntent = this.getIntent();
        sourceLocationId = sourceIntent.getStringExtra("source_location_id");
        destinationLocationId = sourceIntent.getStringExtra("destination_location_id");

        busesGv = (GridView) findViewById(R.id.busesGrid);
        buses = new ArrayList<>();
        adapter = new GridBusesAdapter(this, buses);
        busesGv.setAdapter(adapter);

        // Fetch the buses.
        try {
            fetchBuses();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchBuses() throws JSONException {
        RequestQueue q = Volley.newRequestQueue(this);
        JSONObject postData = new JSONObject();
        postData.put("source_location_id", this.sourceLocationId);
        postData.put("destination_location_id", this.destinationLocationId);

        String url = AppSettings.apiUrl + "/core/buses.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("RESPONSE", response.toString());
                        try {
                            BusesActivity.this.updateBuses(response.getJSONArray("data"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.getMessage() != null)
                            Log.i("RESPONSE", error.getMessage());
                    }
                }
        );

        q.add(request);
    }

    public void updateBuses(JSONArray fetchedBuses) throws JSONException {
        buses.clear();
        for(int i=0; i<fetchedBuses.length(); ++i) {
            buses.add( fetchedBuses.getJSONObject(i) );
        }
        adapter.notifyDataSetChanged();
    }


}
