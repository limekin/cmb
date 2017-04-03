package com.catchmybus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameText, passwordText, confirmText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameText = (EditText) findViewById(R.id.usernameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        confirmText = (EditText) findViewById(R.id.confirmPasswordText);
    }

    public void handleRegister(View view) throws JSONException{
        Log.i("REQUEST", "About to create rq");
        if(! this.validateForm()) return;

        /**
         * Get the required details and make the request.
         */
        String username = usernameText.getText().toString(),
                password = passwordText.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = AppSettings.apiUrl + "/core/register.php";
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.has("error")) {
                                Toast t  = null;

                                    t = Toast.makeText(RegisterActivity.this,
                                            response.getString("error"), Toast.LENGTH_LONG);
                                t.show();
                                return;
                            }

                            Toast t = Toast.makeText(RegisterActivity.this,
                                    response.getString("data"), Toast.LENGTH_LONG);
                            t.show();
                            Intent intent  = new Intent(RegisterActivity.this, LoginActivity.class);
                            RegisterActivity.this.startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }

        );
        queue.add(request);
        Log.i("REQUEST", "Sent the request.");


    }

    public boolean validateForm() {
        String username = usernameText.getText().toString(),
                password = passwordText.getText().toString(),
                confirm = confirmText.getText().toString();

        return true;
    }
}
