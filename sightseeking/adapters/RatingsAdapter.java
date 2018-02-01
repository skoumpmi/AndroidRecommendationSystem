package com.recommender.sightseeing.sightseeking.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.recommender.sightseeing.sightseeking.DTOs.RatingDTO;
import com.recommender.sightseeing.sightseeking.R;
import com.recommender.sightseeing.sightseeking.SightSeeker;
import com.recommender.sightseeing.sightseeking.communicationAPI.ApiActions;
import com.recommender.sightseeing.sightseeking.communicationAPI.DatabaseAPI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by User on 25/7/2015.
 */
public class RatingsAdapter extends ArrayAdapter<RatingDTO> {

    private final Context context;
    private List<RatingDTO> ratings;


    private LatLng placeLL = null;
    private String placeName = "";
    private String vicinity = "";




    public RatingsAdapter(Context context, int resource, List<RatingDTO> ratings) {
        super(context, resource, ratings);

        this.context = context;
        this.ratings = ratings;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.myplace_list_item, parent, false);
        }
            RatingDTO rating = ratings.get(position);
            try {
                String gPlaceId = DatabaseAPI.getResponse(ApiActions.GET_G_PLACE_ID, rating.getItem_id());


                String request = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";
                String url = request + gPlaceId + "&key=AIzaSyCLOfpoI80_UhIXTlSuaewviaqGIGfXLIc";

                new GetGPlaceById().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url); //.get
                        //(2000, TimeUnit.MILLISECONDS);
                TextView title = (TextView) convertView.findViewById(R.id.placeTitle);
                title.setText(placeName);

                TextView desc = (TextView) convertView.findViewById(R.id.placeAddresss);
                desc.setText(vicinity);

                /*float my_rating =  Float.parseFloat(DatabaseAPI.getResponse(ApiActions.GET_RATING, ((SightSeeker) context).getUser_id(), rating.getItem_id()));
                TextView pref = (TextView) convertView.findViewById(R.id.placeRating);
                pref.setText(my_rating + " stars!");*/

            } catch (Exception e) {
                e.printStackTrace();
            }


        return convertView;
    }

    private class GetGPlaceById extends AsyncTask<String, Void, String> {
        //fetch and parse place data

        @Override
        protected String doInBackground(String... placesURL) {

            StringBuilder placesBuilder = new StringBuilder();

            //process search parameter string(s)
            for (String placeSearchURL : placesURL) {
                //execute search
                HttpClient placesClient = new DefaultHttpClient();
                try {
                    //try to fetch the data
                    HttpGet placesGet = new HttpGet(placeSearchURL);
                    HttpResponse placesResponse = placesClient.execute(placesGet);
                    StatusLine placeSearchStatus = placesResponse.getStatusLine();
                    if (placeSearchStatus.getStatusCode() == 200) {
                        //we have an OK response
                        HttpEntity placesEntity = placesResponse.getEntity();
                        BufferedReader placesReader = new BufferedReader(new InputStreamReader(placesEntity.getContent()));
                        String lineIn;
                        while ((lineIn = placesReader.readLine()) != null) {
                            placesBuilder.append(lineIn);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return placesBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            //parse place data returned from Google Places

            try {
                //parse JSON
                JSONObject resultObject = new JSONObject(result);
                JSONObject placeObj = resultObject.getJSONObject("result");
                //loop through places
                //parse each place
                boolean missingValue;

                try {
                    //attempt to retrieve place data values
                    missingValue = false;
                    JSONObject loc = placeObj.getJSONObject("geometry").getJSONObject("location");
                    placeLL = new LatLng(
                            Double.valueOf(loc.getString("lat")),
                            Double.valueOf(loc.getString("lng")));



                    vicinity = placeObj.getString("vicinity");
                    placeName = placeObj.getString("name");

                } catch (JSONException jse) {
                    missingValue = true;
                    jse.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
