package com.catchmybus.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.BusesActivity;
import com.catchmybus.R;
import com.catchmybus.settings.AppSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GridBusesAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<JSONObject> buses;
    public GridBusesAdapter(Context context, ArrayList<JSONObject> buses) {
        this.context = context;
        this.buses = buses;
    }

    @Override
    public int getCount() {
        return buses.size();
    }

    @Override
    public Object getItem(int i) {
        return buses.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
        );
        View ret = inflater.inflate(R.layout.single_bus_grid, null);
        TextView busNameT = (TextView) ret.findViewById(R.id.busName);
        TextView busTypeT = (TextView) ret.findViewById(R.id.busType);
        ImageView busImage = (ImageView) ret.findViewById(R.id.busImage);
        try {
            busNameT.setText(this.buses.get(i).getString("name"));
            busTypeT.setText(this.buses.get(i).getString("bus_type"));
            getBusPreview(busImage, i);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public void getBusPreview(ImageView imageView, int position) throws JSONException {
        RequestQueue q = Volley.newRequestQueue(this.context);
        JSONObject postData = new JSONObject();
        String url = AppSettings.apiUrl + "/core/bus_preview.php?bus_id=" +
                this.buses.get(position).getString("id");
        final ImageView imageVieww = imageView;

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
                            imageVieww.setImageBitmap(imageBitmap);

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
}
