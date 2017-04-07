package com.catchmybus;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.services.GeocodeIntentService;
import com.catchmybus.services.TrackerIntentService;
import com.catchmybus.settings.AppData;
import com.catchmybus.settings.AppSettings;
import com.catchmybus.settings.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private JSONObject bus;
    private GoogleMap currentMap;
    private AddressResultReceiver mResultReceiver;
    private String busId;
    private TextView busIdT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        busId = AppData.get(this, "selected_bus_id");
        mResultReceiver = new AddressResultReceiver(new Handler());
        fetchBusInfo();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        this.currentMap = googleMap;
        this.currentMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this, R.raw.style_map_night));
        //LatLng sydney = new LatLng(-33.852, 151.211);
        //googleMap.addMarker(new MarkerOptions().position(sydney)
        //        .title("Marker in Sydney"));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
                            MapActivity.this.bus = response.getJSONObject("data");
                            MapActivity.this.busInfoCallback();
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

    public void busInfoCallback() throws JSONException {
        // Strat the intent service for converting the location name.
        Intent intent = new Intent(this, GeocodeIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_NAME_EXTRA, bus.getString("current_location"));
        startService(intent);
    }

    public void addMarker(double lat, double lng) throws JSONException {
        this.currentMap.setMinZoomPreference(14);
        LatLng busLocation = new LatLng(lat, lng);
        Marker marker = this.currentMap.addMarker(new MarkerOptions()
                .position(busLocation)
                .title(this.bus.getString("name"))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker)));
        marker.setTag(this.bus.getString("id"));
        busIdT = (TextView) findViewById(R.id.busId);

        // Set marker click listener.
        this.currentMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String busId = (String) marker.getTag();
                MapActivity.this.busIdT.setText(busId);
                return true;
            }
        });
        currentMap.moveCamera(CameraUpdateFactory.newLatLng(busLocation));
    }

    public void startTracking(View view) {
        AppData.put(this, "tracked_bus", this.busId);
        Log.i("TRACKER", "Tracker about to start.");
        Intent trackerIntent = new Intent(this, TrackerIntentService.class);
        startService(trackerIntent);
    }



    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            double lat = resultData.getDouble(Constants.LAT_KEY);
            double lng = resultData.getDouble(Constants.LNG_KEY);
            try {
                MapActivity.this.addMarker(lat, lng);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
