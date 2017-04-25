package com.catchmybus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.adapters.StopsAdapter;
import com.catchmybus.settings.AppData;
import com.catchmybus.settings.AppSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    private Spinner sourceSpinner, destinationSpinner;
    private ArrayList<JSONObject> stops;
    private StopsAdapter stopsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Get the spinners.
        sourceSpinner = (Spinner) this.findViewById(R.id.sourceSpinner);
        destinationSpinner = (Spinner) this.findViewById(R.id.destinationSpinner);

        // Init the stops.
        stops = new ArrayList<JSONObject>();
        this.loadStops();

        // Now link the adapters.
        stopsAdapter = new StopsAdapter(stops, this);
        sourceSpinner.setAdapter(stopsAdapter);
        destinationSpinner.setAdapter(stopsAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadStops() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject data = AppSettings.getAuthJson(this);
        String url = AppSettings.apiUrl + "/core/stops.php";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("error")) {
                                Toast t = Toast.makeText(UserActivity.this, response.getString("error"), Toast.LENGTH_LONG);
                                t.show();
                                return;
                            }
                            UserActivity.this.stopsCallback(response);
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

    public void stopsCallback(JSONObject response) throws JSONException {
        // Get the list and update the json array for stops.
        Log.i("RESPONSE", response.toString());
        JSONArray stopsJsonArray = response.getJSONArray("data");
        this.stops.clear();
        for(int i=0; i<stopsJsonArray.length(); ++i)
            stops.add(stopsJsonArray.getJSONObject(i));
        this.stopsAdapter.notifyDataSetChanged();
    }

    public void searchBuses(View view) throws JSONException {
        // Get the id of the source and desitnation locations.
        JSONObject source = (JSONObject) sourceSpinner.getSelectedItem();
        JSONObject destination = (JSONObject) destinationSpinner.getSelectedItem();
        String sourceLocationId = source.getString("id"),
                desintationLocationId = destination.getString("id");

        Intent intent = new Intent(this, BusesActivity.class);
        AppData.put(this, "source_location_id", sourceLocationId);
        AppData.put(this, "destination_location_id", desintationLocationId);
        startActivity(intent);

    }

}
