package com.recommender.sightseeing.sightseeking;

import android.app.Application;

/**
 * Created by User on 19/6/2015.
 */
public class SightSeeker extends Application {

    private String email;
    private long user_id;
    private boolean offline;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }


}
