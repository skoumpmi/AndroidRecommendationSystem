package com.recommender.sightseeing.sightseeking.ui.fragments;

/**
 * Created by User on 17/6/2015.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.recommender.sightseeing.sightseeking.DTOs.RatingDTO;
import com.recommender.sightseeing.sightseeking.DTOs.RecommendationDTO;
import com.recommender.sightseeing.sightseeking.R;
import com.recommender.sightseeing.sightseeking.SightSeeker;
import com.recommender.sightseeing.sightseeking.adapters.RatingsAdapter;
import com.recommender.sightseeing.sightseeking.communicationAPI.ApiActions;
import com.recommender.sightseeing.sightseeking.communicationAPI.DatabaseAPI;
import com.recommender.sightseeing.sightseeking.ui.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MyPlacesFragment extends ListFragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ListAdapter list_adapter;
    private List<RatingDTO> rating_list;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MyPlacesFragment newInstance(int sectionNumber) {
        MyPlacesFragment fragment = new MyPlacesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MyPlacesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            String my_ratings = DatabaseAPI.getResponse(ApiActions.GET_PREFS, ((SightSeeker) getActivity().getApplicationContext()).getUser_id());

            RatingDTO[] ratings;
            Gson gson = new Gson();
            ratings = gson.fromJson(my_ratings, RatingDTO[].class);

            rating_list = new ArrayList<>();
            for (int i = 0; i < ratings.length - 1; i++)
                rating_list.add(ratings[i]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateDisplay(List<RatingDTO> ratingList) {
        RatingsAdapter adapter = new RatingsAdapter(this.getActivity(), R.layout.fragment_myplaces,
                ratingList);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_myplaces, container, false);

        updateDisplay(rating_list);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }


}