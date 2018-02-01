package com.recommender.sightseeing.sightseeking.DTOs;

/**
 * Created by User on 19/6/2015.
 */
public class PlaceDTO {
    private long placeid;
    private String gPlaceId;
    private String name;
    private double latitude;
    private double longitude;

    public PlaceDTO() {
    }

    public PlaceDTO(String gPlaceId, String name, double latitude, double longitude) {
        this.gPlaceId = gPlaceId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PlaceDTO(long placeid, String gPlaceId, String name, double latitude, double longitude) {
        this.placeid = placeid;
        this.gPlaceId = gPlaceId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getPlaceid() {
        return placeid;
    }

    public void setPlaceid(long placeid) {
        this.placeid = placeid;
    }

    public String getgPlaceId() {
        return gPlaceId;
    }

    public void setgPlaceId(String gPlaceId) {
        this.gPlaceId = gPlaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
