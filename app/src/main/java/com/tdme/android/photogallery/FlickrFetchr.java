package com.tdme.android.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/21.
 */

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";

    private static final String API_KEY = "75a93da0a799b6494b755b3135001300";

    private static final String PAGE = "page";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ":with" + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem.PhotosBean.PhotoBean> fetchItems(Integer page) {

        List<GalleryItem.PhotosBean.PhotoBean> items = new ArrayList<>();

        GalleryItem galleryItem;
        try {
            Log.i(TAG, "____________");
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter(PAGE,page.toString())
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            //JSONObject jsonBody = new JSONObject(jsonString);

           // parseItems(items, jsonBody);

            //parse(items,jsonString,jsonBody);

            galleryItem = parseJson(jsonString,GalleryItem.class);
           items =galleryItem.getPhotos().getPhoto();

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public static <T extends Object> T parseJson(String jsonData, Class<T> clz){
        Gson gson = new Gson();
        return gson.fromJson(jsonData, clz);
    }

    /*private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
            throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setmUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }

    }*/
}
