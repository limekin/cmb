package com.catchmybus.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.catchmybus.R;
import com.catchmybus.settings.AppData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StopsInfoAdapter extends BaseAdapter {
    private ArrayList<JSONObject> stops;
    private Context context;

    public StopsInfoAdapter(ArrayList<JSONObject> stops, Context context) {
        this.stops = stops;
        this.context = context;
    }
    @Override
    public int getCount() {
        return stops.size();
    }

    @Override
    public Object getItem(int i) {
        return stops.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View returnView = inflater.inflate(R.layout.single_stop_info, null);
        TextView stopNameText = (TextView) returnView.findViewById(R.id.stopName);




        TextView timeText = (TextView) returnView.findViewById(R.id.timeTaken);
        // Get the name of the stop at index i.
        try {
            String stopName = this.stops.get(i).getString("location");
            Log.i("RESPONSE", "Stop: " + stopName);
            stopNameText.setText(stopName);
            timeText.setText(this.stops.get(i).getString("time_to_source"));

            if(AppData.get(this.context, "source_location_id").equals(
                    this.stops.get(i).getString("location_id"))) {
                    stopNameText.setTextColor(this.context.getColor(R.color.colorAccent));
            } else if(AppData.get(this.context, "destination_location_id").equals(
                    this.stops.get(i).getString("location_id")))  {
                    stopNameText.setTextColor(this.context.getColor(R.color.colorAccent));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnView;
    }
}
