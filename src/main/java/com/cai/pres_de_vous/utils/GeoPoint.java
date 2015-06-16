package com.cai.pres_de_vous.utils;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by Yoann Diquélou on 30/05/15.
 * Store a point and easy JSON converter
 */
public class GeoPoint {
    // position
    private float longitude;
    private float latitude;
    // search perimeter
    private int perimeter;

    /**
     * Create a Point with latitude, longitude and search perimeter
     * @param lat: latitude
     * @param lng: longitude
     * @param perimeter: search perimeter
     */
    public GeoPoint(float lat, float lng, int perimeter){
        longitude = lng;
        latitude = lat;
        this.perimeter = perimeter;
    }

    /**
     * Create a simple point with latitude and longitude
     * by default perimeter is 500
     * @param lat: latitude
     * @param lng: longitude
     */
    public GeoPoint(float lat, float lng){
        longitude = lng;
        latitude = lat;
        perimeter = 500;
    }


    /**
     * Convert a JSON point to object
     * @param obj
     */
    public GeoPoint(JsonObject obj){
        longitude = obj.getLong("longitude");
        latitude = obj.getLong("latitude");
        if(obj.getInteger("perimetre")!=null){
            perimeter = obj.getInteger("perimetre");
        }else {
            perimeter = 500;
        }
    }

    /**
     * latitude getter
     * @return lat: latitude
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * longitude getter
     * @return lat: longitude
     */
    public float getLongitude() {
        return longitude;
    }

    /**
     * latitude setter
     * @param latitude: latitude
     */
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    /**
     * longitude setter
     * @param longitude: longitude
     */
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    /**
     * perimeter setter
     * @param perimeter
     */
    public void setPerimeter(int perimeter){this.perimeter = perimeter;}

    public int getPerimeter(){return perimeter;}

    public boolean isValid(){
        if(latitude>=-90.0 && latitude<=90.0 && longitude>=-180.0 && longitude<=180.0)return true;
        return false;
    }

    public JsonObject toJSON(){
        JsonObject json = new JsonObject();
        json.putValue("latitude",""+latitude);
        json.putValue("longitude", ""+longitude);
        json.putValue("perimetre",""+perimeter);
        return json;
    }
}
