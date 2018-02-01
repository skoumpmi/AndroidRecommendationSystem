package com.recommender.sightseeing.sightseeking.communicationAPI;

import android.os.AsyncTask;

import com.recommender.sightseeing.sightseeking.SightSeeker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by User on 19/6/2015.
 */
public class RecommendationAPI extends BaseCommunicationAPI{

    public static String getResponse(Object... params) throws Exception {

        // evaluating parameters

        long user_id = Long.parseLong(params[0].toString());
        int howMany = Integer.parseInt(params[1].toString());
        String url = endpoint + "/RecommenderServlet?userID="+user_id+"&howMany="+howMany+"&format=json";

        new doGet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url).get(3000,
                TimeUnit.MILLISECONDS);

        return result;
    }

    private static class doGet extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            if(url[0] != null) {
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

                        result = response.toString();
                        return result;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }
}
