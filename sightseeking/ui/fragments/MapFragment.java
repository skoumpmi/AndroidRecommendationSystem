package com.recommender.sightseeing.sightseeking.ui.fragments;

/**
 * Created by User on 17/6/2015.
 */

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.recommender.sightseeing.sightseeking.DTOs.RecommendationDTO;
import com.recommender.sightseeing.sightseeking.SightSeeker;
import com.recommender.sightseeing.sightseeking.communicationAPI.ApiActions;
import com.recommender.sightseeing.sightseeking.communicationAPI.DatabaseAPI;
import com.recommender.sightseeing.sightseeking.communicationAPI.RecommendationAPI;
import com.recommender.sightseeing.sightseeking.ui.activities.MainActivity;
import com.recommender.sightseeing.sightseeking.R;

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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapFragment extends Fragment{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";


//    private GoogleApiClient mGoogleApiClient;

    private static View rootView;
    private int userIcon, foodIcon, drinkIcon, shopIcon, otherIcon;

    private LatLng storePosition;
    private SupportMapFragment fragment;
    PendingIntent pendingIntent;
    private int mSectionNumber;
    private OnFragmentInteractionListener mListener;
    private GoogleMap map;
    private LatLng myPosition;


    private Marker[] placeMarkers;
    private int MAX_PLACES;
    private MarkerOptions[] places;
    private String[] placeId;
    private HashMap<Marker, String> markerid;

    private int recc_count, recc_ptr;
    private boolean recommender_flag;

    private SharedPreferences sharedPreferences;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MapFragment newInstance(int sectionNumber) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


        userIcon = R.drawable.yellow_point;
        foodIcon = R.drawable.red_point;
        drinkIcon = R.drawable.blue_point;
        shopIcon = R.drawable.green_point;
        otherIcon = R.drawable.purple_point;

        //TODO: change all integers to strings
        MAX_PLACES = Integer.parseInt(sharedPreferences.getString("max_places", "20"));

        placeMarkers = new Marker[MAX_PLACES];
    }

    private void updatePlaces() {
        //get types from preferences or default
        Set<String> def_set= new HashSet<>();
        def_set.add("point_of_interest");
        def_set.add("food");
        def_set.add("park");


        Set<String> type_set = sharedPreferences.getStringSet("places_types", def_set);

        String types = "";


        for(String type : type_set){
            types += type + "|";
        }
        types = types.substring(0,types.length()-1);

        try {
            types = URLEncoder.encode(types, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }


        String placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
                "json?location=" + myPosition.latitude + "," + myPosition.longitude +
                "&radius=50000" +
                "&types=" + types +
                "&key=AIzaSyCLOfpoI80_UhIXTlSuaewviaqGIGfXLIc&sensor=true";

        new GetPlaces().execute(placesSearchStr);
    }

    private void getRecommentedPlaces() {

        long user_id = ((SightSeeker) getActivity().getApplicationContext()).getUser_id();

        recc_count = Integer.parseInt(sharedPreferences.getString("recc_count", "10"));
        recc_ptr = 0;

        try {

            RecommendationDTO[] rcmd;

            String recommendations = RecommendationAPI.getResponse(user_id, 10);

            //manage the returning json string to get only the desired part
            String pre = recommendations.substring(0, recommendations.indexOf('['));
            recommendations = recommendations.replace(pre, "");
            recommendations = recommendations.substring(0, recommendations.length() - 2);

            Gson gson = new Gson();
            rcmd = gson.fromJson(recommendations, RecommendationDTO[].class);

            String request = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";


            if (placeMarkers != null) {
                for (int pm = 0; pm < placeMarkers.length; pm++) {
                    if (placeMarkers[pm] != null)
                        placeMarkers[pm].remove();
                }
            }

            places = new MarkerOptions[recc_count];
            placeId = new String[recc_count];

            markerid= new HashMap<>();



            for (RecommendationDTO recommendationDTO : rcmd) {
                if (recommendationDTO != null) {
                    //get place by this id first get gPlaceId from this ItemId
                    String gPlaceId = DatabaseAPI.getResponse(ApiActions.GET_G_PLACE_ID, recommendationDTO.getId());

                    String url = request + gPlaceId + "&key=AIzaSyCLOfpoI80_UhIXTlSuaewviaqGIGfXLIc";

                    new GetGPlaceById().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url).get
                            (2000, TimeUnit
                            .MILLISECONDS);

                }
            }




        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //f map is drawn do not draw it again
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {
            rootView = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }

        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, fragment).commit();
        }
    }

    public GoogleMap.OnInfoWindowClickListener getInfoWindowClickListener() {
        return new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                long place_id = 0;
                try {
                    place_id = Long.parseLong(DatabaseAPI.getResponse(ApiActions.GET_PLACE_ID, markerid.get(marker)));
                    if (place_id < 0) {
                        String name = marker.getTitle().replace(" - Touch here to Rate!!!", "");
                        String desc = marker.getSnippet();
                        String gpid = markerid.get(marker);
                        double lat = marker.getPosition().latitude;
                        double lng = marker.getPosition().longitude;
                        DatabaseAPI.getResponse(ApiActions.SAVE_PLACE, gpid, name, desc, lat, lng);
                        place_id = Long.parseLong(DatabaseAPI.getResponse(ApiActions.GET_PLACE_ID, gpid));
                    }

                    RatingFragment dialog = RatingFragment.newInstance(mSectionNumber, place_id, marker.getTitle().replace(" - Touch here to Rate!!!", ""));

                    dialog.show(getFragmentManager(), "RatingFragment");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = fragment.getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            map.setOnInfoWindowClickListener(getInfoWindowClickListener());


            //TODO: try with map.getMyLocation() if not working uncomment
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


            //Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);


            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider); //map.getMyLocation(); //

            if (location != null) {

//                Toast.makeText(getActivity(), "lat: " + location.getLatitude(), Toast.LENGTH_LONG).show();

                double latitude = location.getLatitude();

                double longitude = location.getLongitude();

                myPosition = new LatLng(latitude, longitude);

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 100));


                recommender_switch();
            } else {
                Toast.makeText(getActivity(), "location is null", Toast.LENGTH_LONG).show();
            }

            //TODO: setup preferences
//            preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String show_on_map = preferences.getString("Show_on_map","0");
//            String storeid = preferences.getString("StoreId","Not Found!");
//            if (show_on_map.equals("1"))
//            {
//                Toast.makeText(getActivity(), storeid, Toast.LENGTH_SHORT).show();
//                Store route_store=new Select().from(Store.class).where("store_id = ?",storeid).executeSingle();
//                storePosition= new LatLng(route_store.getLatitude(), route_store.getLongitude());
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(storePosition, 17));
//                Routing routing = new Routing(Routing.TravelMode.WALKING);
//                routing.registerListener(this);
//                routing.execute(myPosition, storePosition);
//            }


/*
            Routing routing = new Routing(Routing.TravelMode.WALKING);
            routing.registerListener(this);
            routing.execute(myPosition, storePosition);*/
        }

    }


    public static float getRadius(String key, Context context) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        float radius = (float)preferences.getInt(key,0);
//        return radius;
        return 5000F;
    }

    private void drawCircle(LatLng point, float radius) {
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(radius);

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Border width of the circle
        circleOptions.strokeWidth(1);

        // Adding the circle to the GoogleMap
        map.addCircle(circleOptions);
    }
//
//    private void drawMarker(Store store, Bitmap iconBmp) {
//         Creating an instance of MarkerOptions
//        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(store.getLatitude(), store.getLongitude()))
//                .title(store.getStoreName())
//                .snippet(store.getAddress() + " " + store.getAddressNo() + "\n\n"
//                        + store.getLatitude() + " " + store.getLongitude())
//                .icon(BitmapDescriptorFactory.fromBitmap(iconBmp));
//         Adding marker on the Google Map
//        map.addMarker(markerOptions);
//
//    }

    @Override
    public void onDestroyView() {
//        preferences  = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        preferences.edit().putString("Show_on_map","0").commit();
        super.onDestroyView();
    }

    @Override
    public void onPause() {
//        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        preferences.edit().putString("Show_on_map","0").commit();
        super.onPause();
    }

    @Override
    public void onDetach() {
//        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        preferences.edit().putString("Show_on_map","0").commit();
        super.onDetach();
        mListener = null;
    }

    private void recommender_switch(){

        recommender_flag = sharedPreferences.getBoolean("recommend_flag", true);

        refreshPlaces();
    }

    private void refreshPlaces(){
        if(!recommender_flag)
            updatePlaces();
        else
            getRecommentedPlaces();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onMapFragmentInteraction(boolean recommend_flag);
    }



    private class GetPlaces extends AsyncTask<String, Void, String> {
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
            if (placeMarkers != null) {
                for (int pm = 0; pm < placeMarkers.length; pm++) {
                    if (placeMarkers[pm] != null)
                        placeMarkers[pm].remove();
                }
            }

            try {
                //parse JSON
                JSONObject resultObject = new JSONObject(result);
                JSONArray placesArray = resultObject.getJSONArray("results");
                places = new MarkerOptions[placesArray.length()];
                placeId = new String[placesArray.length()];
                //loop through places
                for (int p = 0; p < placesArray.length(); p++) {
                    //parse each place
                    boolean missingValue = false;
                    LatLng placeLL = null;
                    String placeName = "";
                    String vicinity = "";
                    int currIcon = otherIcon;

                    try {
                        //attempt to retrieve place data values
                        missingValue = false;
                        JSONObject placeObj = placesArray.getJSONObject(p);
                        JSONObject loc = placeObj.getJSONObject("geometry").getJSONObject("location");
                        placeLL = new LatLng(
                                Double.valueOf(loc.getString("lat")),
                                Double.valueOf(loc.getString("lng")));

                        JSONArray types = placeObj.getJSONArray("types");

                        for (int t = 0; t < types.length(); t++) {
                            //what type is it
                            String thisType = types.get(t).toString();

                            if (thisType.contains("food")) {
                                currIcon = foodIcon;
                                break;
                            } else if (thisType.contains("bar")) {
                                currIcon = drinkIcon;
                                break;
                            } else if (thisType.contains("store")) {
                                currIcon = shopIcon;
                                break;
                            } else {
                                currIcon = otherIcon;
                                break;
                            }
                        }

                        vicinity = placeObj.getString("vicinity");
                        placeName = placeObj.getString("name");
                        placeId[p] = placeObj.getString("place_id");

                    } catch (JSONException jse) {
                        missingValue = true;
                        jse.printStackTrace();
                    }

                    if (missingValue)
                        places[p] = null;
                    else
                        places[p] = new MarkerOptions()
                                .position(placeLL)
                                .title(placeName + " - Touch here to Rate!!!")
                                .icon(BitmapDescriptorFactory.fromResource(currIcon))
                                .snippet(vicinity);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

//            Drawable iconDrawable = getResources().getDrawable(R.drawable.abc_btn_radio_to_on_mtrl_000);
//            Bitmap iconBmp = ((BitmapDrawable) iconDrawable).getBitmap();
            LatLng point;
            int reqCode = 1;

            if (places != null && placeMarkers != null) {
                markerid = new HashMap<>();
                for (int p = 0; p < places.length && p < placeMarkers.length; p++) {
                    //will be null if a value was missing
                    if (places[p] != null) {
//                        point = new LatLng(places[p].getPosition().latitude, places[p].getPosition().longitude);
                        // Drawing marker on the map
                        placeMarkers[p] = map.addMarker(places[p]);
                        markerid.put(placeMarkers[p], placeId[p]);
                        // Drawing circle on the map
//                        drawCircle(point,getRadius("notificationRadius",getActivity()));
                        // This intent will call the activity ProximityActivity
//                    Intent proximityIntent = new Intent("com.bargainhunter.activity.proximity");
//
//                    // Passing latitude to the PendingActivity
//                    proximityIntent.putExtra("lat", point.latitude);
//
//                    // Passing longitude to the PendingActivity
//                    proximityIntent.putExtra("lng", point.longitude);
//
//                    proximityIntent.putExtra("storename", store.getStoreName());
//                    proximityIntent.putExtra("reqcode", reqCode);
//                    proximityIntent.putExtra("StoreOffers", stringOffers);
//                    proximityIntent.putExtra("storeaddr", store.getAddress() + " " + store.getAddressNo());
//                    // Creating a pending intent which will be invoked by LocationManager when the specified region is
//                    // entered or exited
//                    pendingIntent = PendingIntent.getActivity(getActivity().getBaseContext(), reqCode, proximityIntent, PendingIntent.FLAG_ONE_SHOT);
                        // Setting proximity alert
                        // The pending intent will fire when the device enters the region x meters
                        // away from the marked point
                        // If -1 , it will not be expired
//                    locationManager.addProximityAlert(point.latitude, point.longitude, getRadius("notificationRadius",getActivity()), -1, pendingIntent);
                        reqCode++;
                    }
                }
            }
        }
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
                LatLng placeLL = null;
                String placeName = "";
                String vicinity = "";
                int currIcon = otherIcon;

                try {
                    //attempt to retrieve place data values
                    missingValue = false;
                    JSONObject loc = placeObj.getJSONObject("geometry").getJSONObject("location");
                    placeLL = new LatLng(
                            Double.valueOf(loc.getString("lat")),
                            Double.valueOf(loc.getString("lng")));

                    JSONArray types = placeObj.getJSONArray("types");

                    for (int t = 0; t < types.length(); t++) {
                        //what type is it
                        String thisType = types.get(t).toString();

                        if (thisType.contains("food")) {
                            currIcon = foodIcon;
                            break;
                        } else if (thisType.contains("bar")) {
                            currIcon = drinkIcon;
                            break;
                        } else if (thisType.contains("store")) {
                            currIcon = shopIcon;
                            break;
                        } else {
                            currIcon = otherIcon;
                            break;
                        }
                    }

                    vicinity = placeObj.getString("vicinity");
                    placeName = placeObj.getString("name");
                    placeId[recc_ptr] = placeObj.getString("place_id");

                } catch (JSONException jse) {
                    missingValue = true;
                    jse.printStackTrace();
                }

                if (missingValue)
                    places[recc_ptr] = null;
                else
                    places[recc_ptr] = new MarkerOptions()
                            .position(placeLL)
                            .title(placeName + " - Touch here to Rate!!!")
                            .icon(BitmapDescriptorFactory.fromResource(currIcon))
                            .snippet(vicinity);

                recc_ptr++;

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (places != null && placeMarkers != null) {
                    //will be null if a value was missing
                    if (places[recc_ptr-1] != null) {
                        // Drawing marker on the map
                        placeMarkers[recc_ptr-1] = map.addMarker(places[recc_ptr-1]);
                        markerid.put(placeMarkers[recc_ptr-1], placeId[recc_ptr-1]);
                    }

            }


        }
    }
}


