package com.catchmybus.storage;

import android.content.Context;
import android.util.Log;

import com.catchmybus.settings.AppSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FavoritesStore {

    private Context context;

    public FavoritesStore(Context context) {
        this.context = context;
    }

    public void addFavorite(JSONObject favorite) {
        FileOutputStream outputStream;
        JSONArray favorites = getFavorites();
        favorites.put(favorite);
        String outputData = favorites.toString();
        Log.i("FAV",outputData.toString());
        try {
            outputStream = context.openFileOutput(AppSettings.favoritesFile,
                    (checkFileExists() ? Context.MODE_APPEND : Context.MODE_PRIVATE));
            outputStream.write(outputData.getBytes());
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getFavorites() {
        FileInputStream inputStream;
        JSONArray favorites = new JSONArray();
        String jsonInputString = "";
        byte buffer[] = new byte[1024];

        try {
            inputStream = context.openFileInput(AppSettings.favoritesFile);
            int readCount;
            int bytesRead = 0;
            String readString;
            while((readCount = inputStream.read(buffer, 0, 1024)) > 0) {
                bytesRead += readCount;
                readString = new String(buffer);
                jsonInputString += readString;
                Log.i("FAV", jsonInputString);
            }
            Log.i("FAV",  String.valueOf(bytesRead));
            favorites = new JSONArray(jsonInputString);
            inputStream.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return favorites;
    }


    public boolean checkFileExists() {
        String files[] = context.fileList();
        for(int i=0; i<files.length; ++i) {
            if(files[i].equals(AppSettings.favoritesFile))
                return true;
        }
        return false;
    }


}
