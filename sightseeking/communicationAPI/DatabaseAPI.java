package com.recommender.sightseeing.sightseeking.communicationAPI;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/**
 * Created by User on 18/6/2015.
 */
public class DatabaseAPI extends BaseCommunicationAPI {

    private static String saveNewUser(String login, String passkey) {

        String request = "/HandlerServlet?method=new&login=" + login
                + "&passkey=" + passkey;

        return endpoint + request;
    }

    private static String login(String login, String passkey) {
        String request = "/HandlerServlet?method=connect&login=" + login
                + "&passkey=" + passkey;
        return endpoint + request;
    }

    private static String updateUserLocation(long user_id, double lat, double lng) {
        String request = "/HandlerServlet?method=setLatLng&user_id=" + user_id
                + "&lat=" + lat
                + "&lng=" + lng;
        return endpoint + request;
    }

    private static String saveRating(long user_id, long place_id, double rating) {
        String request = "/HandlerServlet?method=storePref&user_id=" + user_id
                + "&item_id=" + place_id
                + "&pref=" + rating;

        return endpoint + request;
    }

    private static String getRating(long user_id, long place_id) {

        String request = "/HandlerServlet?method=getPref&user_id=" + user_id
                + "&item_id=" + place_id;

        return endpoint + request;
    }

    private static String getPlaceId(String gPlaceId) {

        String request = "/HandlerServlet?method=getItemId&gPlace_id=" + gPlaceId;
        return endpoint + request;
    }

    private static String getGPlaceId(String placeId) {

        String request = "/HandlerServlet?method=getGPlaceId&item_id=" + placeId;
        return endpoint + request;
    }

    private static String savePlace(String gPlace_id, String name, String description, Double latitude, Double longitude) throws UnsupportedEncodingException {
        String request = "/HandlerServlet?method=saveItem&gPlace_id=" + gPlace_id
                + "&name=" +  URLEncoder.encode(name, "UTF-8")
                + "&description=" +  URLEncoder.encode(description, "UTF-8")
                + "&latitude=" + latitude
                + "&longitude=" + longitude;
        return endpoint + request;
    }

    private static String getPrefs(long user_id) throws UnsupportedEncodingException {
        String request = "/HandlerServlet?method=getPrefs&user_id=" + user_id;
        return endpoint + request;
    }

    public static String getResponse(ApiActions action, Object... params) throws Exception {
        //base url declaration
        String url = null;

        // fields to be used
        String login;
        String passkey;

        long user_id;
        long place_id;
        String gPlace_id;
        double rating;

        String name, description;
        Double latitude, longitude;

        switch (action) {
            case SAVE_USER:
                login = params[0].toString();
                passkey = params[1].toString();

                url = saveNewUser(login, passkey);
                break;
            case LOGIN:
                login = params[0].toString();
                passkey = params[1].toString();

                url = login(login, passkey);
                break;
            case UPDATE_USER_LOCATION:
                user_id = Long.parseLong(params[0].toString());
                latitude = Double.parseDouble(params[1].toString());
                longitude = Double.parseDouble(params[2].toString());

                url = updateUserLocation(user_id, latitude, longitude);
                break;
            case SAVE_RATING:
                user_id = Long.parseLong(params[0].toString());
                place_id = Long.parseLong(params[1].toString());
                rating = Double.parseDouble(params[2].toString());

                url = saveRating(user_id, place_id, rating);
                break;
            case GET_RATING:
                user_id = Long.parseLong(params[0].toString());
                place_id = Long.parseLong(params[1].toString());

                url = getRating(user_id, place_id);
                break;
            case GET_PLACE_ID:
                gPlace_id = params[0].toString();

                url = getPlaceId(gPlace_id);
                break;
            case GET_G_PLACE_ID:
                gPlace_id = params[0].toString();

                url = getGPlaceId(gPlace_id);
                break;
            case SAVE_PLACE:
                gPlace_id = params[0].toString();
                name = params[1].toString();
                description = params[2].toString();
                latitude = Double.parseDouble(params[3].toString());
                longitude = Double.parseDouble(params[4].toString());

                url = savePlace(gPlace_id, name, description, latitude, longitude);
                break;
            case GET_PREFS:
                user_id =  Long.parseLong(params[0].toString());

                url = getPrefs(user_id);
                break;
            default:
                break;

        }

        // add format parameter to base http request
        url += "&format=json";

        if (action == ApiActions.SAVE_USER || action == ApiActions.LOGIN) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                // optional default is GET

                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", USER_AGENT);

                int responseCode = con.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();


                    result = response.toString();
                    if (result.contains("return")) {
                        JSONObject json = new JSONObject(result);
                        result = json.get("return").toString();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            result = new doGet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url).get(2000,
                    TimeUnit.MILLISECONDS);
        }


        return result;
    }

    private static class doGet extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            if (url[0] != null) {
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(url[0]).openConnection();
                    // optional default is GET

                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", USER_AGENT);

                    int responseCode = con.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        String result = response.toString();

                        try {
                            if (result.contains("return")) {
                                JSONObject json = new JSONObject(result);
                                result = json.get("return").toString();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return result;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

}
