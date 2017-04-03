package com.catchmybus.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.catchmybus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ADMIN on 16/3/2017.
 */
public class StopsAdapter extends BaseAdapter {


    private ArrayList<JSONObject> stops;
    private Context context;

    public StopsAdapter(ArrayList<JSONObject> stops, Context context) {
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
        View returnView = inflater.inflate(R.layout.stops_view, null);
        TextView stopNameText = (TextView) returnView.findViewById(R.id.stopName);
        // Get the name of the stop at index i.
        try {
            String stopName = this.stops.get(i).getString("location");
            Log.i("RESPONSE", "Stop: " + stopName);
            stopNameText.setText(stopName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return returnView;
    }
}
