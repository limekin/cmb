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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.catchmybus.settings.AppSettings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the user is already logged in.
        checkAuth();
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

}
