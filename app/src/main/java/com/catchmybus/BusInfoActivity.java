package com.catchmybus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.adapters.StopsInfoAdapter;
import com.catchmybus.settings.AppData;
import com.catchmybus.settings.AppSettings;
import com.catchmybus.storage.FavoritesStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BusInfoActivity extends AppCompatActivity {

    private String busId;
    private JSONObject bus;
    private ImageView busImageV;
    private ListView stopListLv;
    private String sourceLocationId;
    private int fare;


    private ArrayList<JSONObject> stops;
    private StopsInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        busId = AppData.get(this, "selected_bus_id");

        busImageV = (ImageView) findViewById(R.id.busImage);
        stops = new ArrayList<>();
        adapter = new StopsInfoAdapter(stops, this);
        stopListLv = (ListView) findViewById(R.id.stopsList);
        stopListLv.setAdapter(adapter);

        sourceLocationId = AppData.get(this, "source_location_id");
        fetchBusInfo();
        fetchBusImage();
        loadStops();
        updateSourceAndDest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bus_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                AppData.put(this, "user_id", "none");
                AppData.put(this, "token", "none");
                AppData.put(this, "user_type", "none");

                Toast.makeText(this, "You have been logged out of your account.", Toast.LENGTH_SHORT)
                        .show();
                // Now send to home page.
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.favorite:
                JSONObject favorite = new JSONObject();
                try {
                    favorite.put("name", bus.getString("name"));
                    favorite.put("source_location", AppData.get(this, "source_location"));
                    favorite.put("destination_location", AppData.get(this, "destination_location"));
                    favorite.put("source_location_id", AppData.get(this, "source_location_id"));
                    favorite.put("destination_location_id", AppData.get(this, "destination_location_id"));
                    favorite.put("fare", this.fare);
                    FavoritesStore favStore = new FavoritesStore(this);
                    favStore.addFavorite(favorite);
                    Toast.makeText(this, "This bus has been added to your favorites successfully.", Toast.LENGTH_SHORT)
                            .show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void fetchBusInfo() {
        Log.i("REQUEST", "Creating the request ..");

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = AppSettings.apiUrl + "/core/bus_info.php?bus_id=" + this.busId ;
        JSONObject data = new JSONObject();
        try {
            data.put("bus_id", this.busId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            BusInfoActivity.this.bus = response.getJSONObject("data");
                            BusInfoActivity.this.fetchFare();
                            BusInfoActivity.this.displayBusInfo();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.getMessage() != null)
                            Log.i("RESPONSE", error.getMessage());
                    }
                }
        );

        // Send the request.
        queue.add(request);
        Log.i("REQUEST", "Request send");
    }

    public void fetchBusImage() {
        RequestQueue q = Volley.newRequestQueue(this);
        String url = AppSettings.apiUrl + "/core/bus_preview.php?bus_id=" +
                this.busId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("RESPONSE", response.toString());
                        try {
                            byte[] bytes = Base64.decode(response.getString("data"), 0);
                            Bitmap imageBitmap = BitmapFactory.decodeByteArray(
                                    bytes,
                                    0,
                                    bytes.length
                            );
                            BusInfoActivity.this.busImageV.setImageBitmap(imageBitmap);

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

    public void fetchFare() {
        RequestQueue q = Volley.newRequestQueue(this);
        String url = AppSettings.apiUrl + "/core/get_fare.php";
        try {
            url += "?bus_type_id=" + this.bus.getString("bus_type_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        url += "&source=" + AppData.get(this, "sourceLocation");
        url += "&destination=" + AppData.get(this, "destinationLocation");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            TextView busFareT = (TextView) BusInfoActivity.this.findViewById(R.id.busFare);
                            busFareT.setText(response.getString("data") + " Rs");
                            BusInfoActivity.this.fare = fare;
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
    @Override
    protected void onStart() {
        super.onStart();
    }

    public void loadStops() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject data = AppSettings.getAuthJson(this);
        String url = AppSettings.apiUrl + "/core/stops_with_time.php";
        url += "?bus_id=" + this.busId + "&location_id=" + this.sourceLocationId;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            BusInfoActivity.this.stopsCallback(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.getMessage() != null)
                            Log.i("RESPONSE", error.getMessage());
                    }
                }
        );

        // Send the request.
        queue.add(request);
    }

    public void displayBusInfo() throws JSONException {
        TextView busNameT = (TextView) findViewById(R.id.busName);
        TextView busTypeT = (TextView) findViewById(R.id.busType);
        busNameT.setText(this.bus.getString("name"));
        busTypeT.setText(this.bus.getString("bus_type"));
    }

    public void stopsCallback(JSONObject response) throws JSONException {
        JSONArray newStops = response.getJSONArray("data");
        this.stops.clear();

        for (int i=0; i<newStops.length(); ++i)
            this.stops.add(newStops.getJSONObject(i));

        this.adapter.notifyDataSetChanged();
    }

    public void showInMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    public void updateSourceAndDest() {
        TextView sourceT = (TextView) findViewById(R.id.sourceLocation)
                ,destinationT = (TextView) findViewById(R.id.destinationLocation);
        sourceT.setText(AppData.get(this, "source_location"));
        destinationT.setText(AppData.get(this, "destination_location"));
    }
}
