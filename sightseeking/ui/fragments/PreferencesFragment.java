package com.recommender.sightseeing.sightseeking.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.recommender.sightseeing.sightseeking.R;

/**
 * Created by User on 22/7/2015.
 */
public class PreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }


}
