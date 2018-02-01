package com.recommender.sightseeing.sightseeking.DTOs;

/**
 * Created by User on 19/6/2015.
 */
public class RatingDTO {

    private long user_id;
    private long item_id;
    private double preference;

    public RatingDTO() {
    }

    public RatingDTO(long user_id, long item_id, double preference) {
        this.user_id = user_id;
        this.item_id = item_id;
        this.preference = preference;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public long getItem_id() {
        return item_id;
    }

    public void setItem_id(long item_id) {
        this.item_id = item_id;
    }

    public double getPreference() {
        return preference;
    }

    public void setPreference(double preference) {
        this.preference = preference;
    }
}
