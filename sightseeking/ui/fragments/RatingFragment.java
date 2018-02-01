package com.recommender.sightseeing.sightseeking.ui.fragments;

/**
 * Created by User on 18/6/2015.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.recommender.sightseeing.sightseeking.R;
import com.recommender.sightseeing.sightseeking.SightSeeker;
import com.recommender.sightseeing.sightseeking.communicationAPI.ApiActions;
import com.recommender.sightseeing.sightseeking.communicationAPI.DatabaseAPI;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.recommender.sightseeing.sightseeking.ui.fragments.RatingFragment.OnDialogRatingFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RatingFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class RatingFragment  extends DialogFragment{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_PLACE_ID = "place_id";
    private static final String ARG_PLACE_NAME = "place_name";

    private int mSectionNumber;
    private long placeId;
    private String placeName;
//
//    private ExpandableListView mExpandableListView;
//
//    // check states!
//    HashMap<Integer, boolean[]> mChildCheckStates;

    private OnDialogRatingFragmentInteractionListener mListener;

    public static RatingFragment newInstance(int sectionNumber, long placeId, String placeName) {
        RatingFragment fragment = new RatingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putLong(ARG_PLACE_ID, placeId);
        args.putString(ARG_PLACE_NAME, placeName);
        fragment.setArguments(args);
        return fragment;
    }
    public RatingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null)
            return;

        int dialogWidth = getResources().getDisplayMetrics().widthPixels;
        int dialogHeight = getResources().getDisplayMetrics().heightPixels - 50;

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            placeId = getArguments().getLong(ARG_PLACE_ID);
            placeName = getArguments().getString(ARG_PLACE_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating_dialog, container, false);

        // Title of the dialog
        getDialog().setTitle("Rate " + placeName);

        final RatingBar ratingbar = (RatingBar) view.findViewById(R.id.ratingBar);


        // set the previous user's rating for the selected place
        //TODO: get rating comes rounded
        try {
            float rating;
            if(((SightSeeker) getActivity().getApplicationContext()).isOffline())
                rating = 3.5F; // use for testing offline
            else
                rating =  Float.parseFloat(DatabaseAPI.getResponse(ApiActions.GET_RATING, ((SightSeeker) getActivity().getApplicationContext()).getUser_id(), placeId));
            if(rating >= 0F )
                ratingbar.setRating(rating);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Button mCancelButton = (Button) view.findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        Button mConfirmButton = (Button) view.findViewById(R.id.rateButton);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDialogRatingFragmentInteraction(placeName,placeId);

                    long user_id = ((SightSeeker) getActivity().getApplicationContext()).getUser_id();

                    try {
                        DatabaseAPI.getResponse(ApiActions.SAVE_RATING, user_id, placeId, ratingbar.getRating());


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    getDialog().dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDialogRatingFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDialogRatingFragmentInteractionListener {
        public void onDialogRatingFragmentInteraction(String name, long place_id);
    }
}
