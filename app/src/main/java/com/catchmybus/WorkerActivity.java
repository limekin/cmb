package com.catchmybus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.dialogs.TimePickerFragment;
import com.catchmybus.settings.AppData;
import com.catchmybus.settings.AppSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class WorkerActivity extends AppCompatActivity {

    private Switch runModeSwitch;
    private String busId;
    private String currentMode;
    private int delaySeconds = -1;
    private EditText descEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

        fetchAssignedBusDetails();
        descEdit= (EditText) findViewById(R.id.description);
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

    public void fetchAssignedBusDetails() {
        RequestQueue q = Volley.newRequestQueue(this);
        String workerLoginId = AppData.get(this, "user_id");

        String url = AppSettings.apiUrl + "/core/worker/assigned_bus.php";
        url += "?worker_id=" + workerLoginId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            WorkerActivity.this.assignedBusCallback(response);
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
    }

    public void assignedBusCallback(JSONObject response) throws JSONException {
        if(response.has("error")) {
            Log.i("WORKER", response.getString("error"));
        } else {
            response = response.getJSONObject("data");
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
            );

            // Set the assigned bus id.
            busId = response.getString("id");
            currentMode = response.getString("run_mode");

            View busInfoView = inflater.inflate(R.layout.worker_bus_info, null);
            TextView busName = (TextView) busInfoView.findViewById(R.id.busName);
            TextView busType = (TextView) busInfoView.findViewById(R.id.busType);
            TextView busRunMode = (TextView) busInfoView.findViewById(R.id.busRunMode);

            busName.setText(response.getString("name"));
            busType.setText(response.getString("bus_type"));
            busRunMode.setText(response.getString("run_mode"));

            // Remove the progress bar.
            ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
            ViewGroup parent =  (ViewGroup) progressBar.getParent();
            parent.removeView(progressBar);

            // Now insert the new view into the ontainer.
            setRunModeHandlers(busInfoView);
            RelativeLayout infoContainer = (RelativeLayout) findViewById(R.id.infoContainer);
            infoContainer.addView(busInfoView);
        }
    }

    // Handles the toggle of run mode.s
    public void setRunModeHandlers(View view) {
        runModeSwitch = (Switch) view.findViewById(R.id.runModeSwitch);
        runModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(currentMode.equals("DEPART"))
                    currentMode = "RETURN";
                else
                    currentMode = "DEPART";
                Log.i("WORKER", "New run mode: " + currentMode);
                WorkerActivity.this.updateRunMode(currentMode);
            }
        });
    }

    // Updates the run mode of the bus.
    public void updateRunMode(String newRunMode) {
        RequestQueue q = Volley.newRequestQueue(this);
        String url = AppSettings.apiUrl + "/core/worker/update_run_mode.php";
        url += "?bus_id=" + busId;
        url += "&run_mode=" + newRunMode;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(WorkerActivity.this, "The run mode has been updated.", Toast.LENGTH_LONG)
                                .show();
                        // Also update it on the view.
                        TextView busRunMode = (TextView) WorkerActivity.this.findViewById(R.id.busRunMode);
                        busRunMode.setText(currentMode);

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
    }

    public void updateDelay(View view) {
        if(! validate()) return;

        RequestQueue q = Volley.newRequestQueue(this);
        String url = AppSettings.apiUrl + "/core/worker/report_delay.php";
        JSONObject postData = new JSONObject();

        try {
            postData.put("bus_id", busId);
            postData.put("delay_description", descEdit.getText().toString());
            postData.put("delay_time", delaySeconds);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(WorkerActivity.this, "The run mode has been updated.", Toast.LENGTH_LONG)
                                .show();
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
    }

    // Shows the time picker.
    public void showTimePicker(View view) {
        DialogFragment dialogFragment = new TimePickerFragment();
        dialogFragment.show(this.getSupportFragmentManager(), "timePicker");
    }

    public int convertSecondsToMinutes(int seconds) {
        int minutes = seconds/60;

        return minutes;
    }
    public void setTime(int hourOfDay, int minute) {
        Date currentTime = Calendar.getInstance().getTime();
        int currentHour = currentTime.getHours();
        int currentMinute = currentTime.getMinutes();

        // Now compute the differnece between the times.
        int seconds = Math.abs((currentHour*3600) + (currentMinute*60) - (hourOfDay*3600 + minute*60));
        this.delaySeconds  = seconds;

        // Update it on the view.
        TextView delayLabel = (TextView) findViewById(R.id.timeLabel);
        delayLabel.setText("Estimated dalay: "
                + String.valueOf(convertSecondsToMinutes(seconds))
                + " minutes.");
    }

    public boolean validate() {
        String description = descEdit.getText().toString();

        if(description.trim().isEmpty()) {
            Toast.makeText(WorkerActivity.this, "You haven't written any description yet.", Toast.LENGTH_LONG)
                .show();
            return false;
        }

        if(delaySeconds == -1) {
            Toast.makeText(WorkerActivity.this, "Please pick the delay time.", Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        return true;
    }

}
