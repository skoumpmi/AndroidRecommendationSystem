package com.recommender.sightseeing.sightseeking.DTOs;

import java.util.List;

/**
 * Created by User on 19/6/2015.
 */
public class UserDTO {

    private long user_id;
    private String login;
    private Double latitude;
    private Double longitude;
    private List<RatingDTO> ratings;


    public UserDTO() {
    }

    public UserDTO(long user_id, String login, List<RatingDTO> ratings) {
        this.user_id = user_id;
        this.login = login;
        this.ratings = ratings;
    }


    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<RatingDTO> getRatings() {
        return ratings;
    }

    public void setRatings(List<RatingDTO> ratings) {
        this.ratings = ratings;
    }
}
