package com.catchmybus.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.util.Pair;

import com.catchmybus.settings.Constants;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocodeIntentService extends IntentService {

    protected ResultReceiver mReceiver;

    public GeocodeIntentService() {
        super("geocoding");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = "";

        // Get the location passed to this service through an extra.
        String locationName = intent.getStringExtra(
                Constants.LOCATION_NAME_EXTRA);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        List<Address> addresses = null;

        try {
            Log.i("LOCATION", locationName);
            addresses = geocoder.getFromLocationName(locationName, 3);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Cannto geocode the result, network issue.";
            Log.e("LOCATION", errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid arguments given for geocoding.";
            Log.e("LOCATION", errorMessage);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                Log.e("LOCATION", "No addresses were found.");
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, -1, -1);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            Log.i("LOCATION", "Address found.");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    address.getLatitude(), address.getLongitude());
        }
    }

    private void deliverResultToReceiver(int resultCode, double lat, double lng) {
        Bundle bundle = new Bundle();
        bundle.putDouble(Constants.LAT_KEY, lat);
        bundle.putDouble(Constants.LNG_KEY, lng);
        mReceiver.send(resultCode, bundle);
    }

}
