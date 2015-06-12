package com.cai.pres_de_vous.utils;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by crocus on 30/05/15.
 */
public class GeoPoint {
    private float longitude;
    private float latitude;
    private String insta_token;

    public GeoPoint(float lat, float lng){
        longitude = lng;
        latitude = lat;
        System.out.println("New GeoPoint");
    }

    public GeoPoint(JsonObject obj){
        longitude = obj.getLong("longitude");
        latitude = obj.getLong("latitude");
        System.out.println("New GeoPoint from JSON");
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setInstaToken(String token){insta_token = token;}

    public String getInstaToken(){return insta_token;}

    public boolean isValid(){
        if(latitude>=-90.0 && latitude<=90.0 && longitude>=-180.0 && longitude<=180.0)return true;
        return false;
    }

    public JsonObject toJSON(){
        JsonObject json = new JsonObject();
        json.putValue("latitude",latitude+"");
        json.putValue("longitude", longitude+"");
        json.putString("insta_token",insta_token);
        return json;
    }
}
