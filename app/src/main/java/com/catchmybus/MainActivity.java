package com.catchmybus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.catchmybus.settings.AppSettings;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText usernameE, passwordE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the user is already logged in.
        checkAuth();
        setRefs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        this.startActivity(intent);
    }

    public void showRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        this.startActivity(intent);
    }

    // CHecks if the user is already logged in.
    public void checkAuth() {
        SharedPreferences pref = this.getSharedPreferences(AppSettings.prefFile,
                Context.MODE_PRIVATE);
        if(! pref.getString("token", "none").equals("none")) {
            String userType = pref.getString("user_type", "none");
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

    public void setRefs() {
        usernameE = (EditText) findViewById(R.id.username);
        passwordE = (EditText) findViewById(R.id.password);
    }

    public void handleLogin(View view) {
        if(! validate()) return;
        String username = usernameE.getText().toString(),
                password = passwordE.getText().toString();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = AppSettings.apiUrl + "/core/login.php";
        JSONObject data = new JSONObject();
        try {
            data.put("password", password);
            data.put("username", username);
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
                            if (response.has("error")) {
                                Toast t = Toast.makeText(MainActivity.this, response.getString("error"), Toast.LENGTH_LONG);
                                t.show();
                                return;
                            }

                            MainActivity.this.loginCallback(response);
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

    public boolean validate() {
        String username = usernameE.getText().toString(),
                password = passwordE.getText().toString();

        if(username.trim().isEmpty()) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.container),
                    "Your username is empty.",
                    Snackbar.LENGTH_LONG);
            snackbar.show();
            return false;
        }

        if(password.trim().isEmpty()) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.container),
                    "Your password is empty.",
                    Snackbar.LENGTH_LONG);
            snackbar.show();
            return false;
        }

        return true;
    }



}
