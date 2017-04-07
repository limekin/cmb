package com.catchmybus.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.R;
import com.catchmybus.UserActivity;
import com.catchmybus.settings.AppData;
import com.catchmybus.settings.AppSettings;

import org.json.JSONException;
import org.json.JSONObject;

public class TrackerIntentService extends IntentService {

    private String selectedBusId;
    private String sourceLocationId;
    private boolean locationReached = false;
    public TrackerIntentService() {
        super("tracker");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int interval = 5000;
        selectedBusId = AppData.get(this.getApplicationContext(), "selected_bus_id");
        sourceLocationId = AppData.get(this.getApplicationContext(), "source_location_id");

        Log.i("TRACKER", "The tracker service has started.");
        while(locationReached == false) {
            RequestQueue q = Volley.newRequestQueue(this.getApplicationContext());
            String url = AppSettings.apiUrl + "/core/get_current_location.php?bus_id=" + this.selectedBusId ;
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String currentLocationId = response.getJSONObject("data")
                                    .getString("current_location_id");
                                TrackerIntentService.this.checkLocationStatus(currentLocationId);
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
            q.add(request);

            // Sleep a bit to avoid overloading.
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkLocationStatus(String currentLocationId) {
        if(currentLocationId.equals(this.sourceLocationId)) {
            Log.i("TRACKER", "Location reached.");

            Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.bus_marker)
                            .setContentTitle("CatchMyBus - Alert")
                            .setContentText("The bus you have requested to track has reached your desired stop.")
                            .setSound(soundUri);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, UserActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(UserActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());
            this.locationReached = true;
        }
    }

}

