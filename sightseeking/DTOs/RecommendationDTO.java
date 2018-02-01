package com.recommender.sightseeing.sightseeking.DTOs;


/**
 * Created by User on 19/6/2015.
 */
public class RecommendationDTO {

    private double value;
    private long id;

    public RecommendationDTO() {
    }

    public RecommendationDTO(double value, long id) {
        this.value = value;
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
