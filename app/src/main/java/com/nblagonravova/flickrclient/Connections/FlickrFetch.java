package com.nblagonravova.flickrclient.Connections;


import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.nblagonravova.flickrclient.Model.GalleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlickrFetch {
    private static  final  String TAG = "FlickrFetch";
    private static final String API_KEY = "50bf79df9e92dacf0d93f6d9fc56f372";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";

    public static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();

        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    private String bulldUrl(String method, String query, int page){
        Uri.Builder uriBilder = ENDPOINT.buildUpon().appendQueryParameter("method", method);
        if (method.equals(FETCH_RECENTS_METHOD)){
            uriBilder.appendQueryParameter("page", String.valueOf(page))
                     .appendQueryParameter("per_page", "60");
        }
        if (method.equals(SEARCH_METHOD)){
            uriBilder.appendQueryParameter("text", query);
        }

        return uriBilder.build().toString();
    }

    private List<GalleryItem> downloadGalleryItems(String url){
        List<GalleryItem> items = null;
        try{
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            items = parseItems(jsonBody);
        }catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items", ioe);
        }catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return items;
    }

    public List<GalleryItem> fetchRecentPhotos(int page){
        String url = bulldUrl(FETCH_RECENTS_METHOD, null, page);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query){
        String url = bulldUrl(SEARCH_METHOD, query, 0);
        return downloadGalleryItems(url);
    }


    private List<GalleryItem> parseItems(JSONObject jsonBody)
            throws IOException, JSONException {
        List<GalleryItem> items = new ArrayList<>();
        Gson gson = new Gson();
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        return  items = Arrays.asList(gson.fromJson(photoJsonArray.toString(), GalleryItem[].class));
    }


}
