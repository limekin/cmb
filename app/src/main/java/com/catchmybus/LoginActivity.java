package com.catchmybus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.settings.AppSettings;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * Get the references to the edittexts used in the form.
         */
        usernameText = (EditText) this.findViewById(R.id.usernameText);
        passwordText = (EditText) this.findViewById(R.id.passwordText);
    }

    public void handleLogin(View view) {
        Log.i("REQUEST", "Creating the request ..");
        /**
         * Get the values from the editexts.
         */
        String username = this.usernameText.getText().toString(),
                password = this.passwordText.getText().toString();

        /**
         * Make and create the request.
         */
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = AppSettings.apiUrl + "/core/login.php";
        JSONObject data = new JSONObject();
        try {
            data.put("password", password);
            data.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**
         * Create the request.
         */
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("error")) {
                                Toast t = Toast.makeText(LoginActivity.this, response.getString("error"), Toast.LENGTH_LONG);
                                t.show();
                                return;
                            }

                            LoginActivity.this.loginCallback(response);
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

    // Save the user id and token.
    public void loginCallback(JSONObject data) throws JSONException {
        String token = data.getJSONObject("data").getString("token");
        String userId = data.getJSONObject("data").getString("user_id");
        String userType = data.getJSONObject("data").getString("user_type");

        SharedPreferences pref = this.getSharedPreferences(AppSettings.prefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token);
        editor.putString("user_id", userId);
        editor.putString("user_type", userType);
        editor.commit();

        Intent intent = null;
        switch(userType) {
            case "user":
                intent = new Intent(this, UserActivity.class);
                break;
            case "worker":
                intent = new Intent(this, WorkerActivity.class);
                break;
        }
        this.startActivity(intent);
    }
}