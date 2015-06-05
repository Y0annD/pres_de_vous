package com.cai.pres_de_vous.utils;


import org.vertx.java.core.json.JsonObject;

import java.util.Date;
import java.util.UUID;

/**
 * User class
 */
public class User {

    public static long TOKEN_VALIDITY = 10000;


    private String lastname;
    private String firstname;
    private String encrypted_password;
    private String insta_key;
    private String google_key;
    private String token;
    // unused because i don't want to complexify this short project
    private long validity;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    /**
     * Check if token is valid and if his associate time is valid too
     * @param test_token: param to test
     * @return true: token is valid, false: token invalid
     */
    public boolean token_is_valid(String test_token){
        Date d = new Date();
        if(token.equals(test_token) && d.getTime()<validity){
            return true;
        }
        return false;
    }

    public void setNewValidity(){

    }

    public User(String firstName, String lastName, String encryptedPassword){
        firstname = firstName;
        lastname = lastName;
        encrypted_password = encryptedPassword;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstName() {
        return firstname;
    }

    public void setFirstName(String firstName) {
        this.firstname = firstName;
    }

    public String getEncrypted_password() {
        return encrypted_password;
    }

    public void setEncrypted_password(String encrypted_password) {
        this.encrypted_password = encrypted_password;
    }

    public String getInsta_key() {
        return insta_key;
    }

    public void setInsta_key(String insta_key) {
        this.insta_key = insta_key;
    }

    public String getGoogle_key() {
        return google_key;
    }

    public void setGoogle_key(String google_key) {
        this.google_key = google_key;
    }

    public void setToken(){
        token = UUID.randomUUID().toString();
        /*Date d = new Date();
        validity = d.getTime()+TOKEN_VALIDITY;*/
    }


    /**
     *
     *@return Return MongoDB request to insert a user
     */
    public JsonObject registerUserRequest(){
        // get date to set token duration
        //Date d = new Date();
        //validity = d.getTime()+TOKEN_VALIDITY;
        setToken();
        // create request object
        JsonObject req = new JsonObject();
        req.putString("action","save");
        req.putString("collection","users");
        req.putObject("document",toJSON());
        return req;

    }

    /**
     * Return user in JSON format
     * @return user in JSON format
     */
    public JsonObject toJSON(){
        JsonObject user = new JsonObject();
        user.putString("firstName",firstname);
        user.putString("lastName",lastname);
        user.putString("password",encrypted_password);
        user.putString("insta_key",insta_key);
        user.putString("google_key",google_key);
        user.putString("token",token);
        //user.putString("token_validity",validity+"");
        return user;
    }

    /**
     * Create MongoDB request to find wether user exist or not
     * @return: JSonObject mongodb request
     */
    public JsonObject findRequest(){
        JsonObject req = new JsonObject();
        JsonObject usr = new JsonObject();
        usr.putString("firstName",firstname);
        usr.putString("lastName",lastname);
        req.putString("action","find");
        req.putString("collection","users");
        req.putObject("matcher",usr);
        return req;
    }

    /**
     * set token for user in mongo DB
     * @return: JSOn request for mongo db
     */
    public JsonObject setTokenRequest(){
        JsonObject req = new JsonObject();
        JsonObject usr = new JsonObject();
        usr.putString("firstName",firstname);
        usr.putString("lastName",lastname);
        req.putString("action","update");
        req.putString("collection","users");
        req.putObject("Criteria", usr);
        JsonObject n = new JsonObject("{'token':'"+token+"'}");
        req.putObject("$set",n);
        return req;
    }


    /**
     * Create MongoDB request to find wether user can login or not
     * @return
     */
    public JsonObject sign_in(){
        JsonObject req = new JsonObject();
        JsonObject usr = new JsonObject();
        //setToken();
        usr.putString("firstName",firstname);
        usr.putString("lastName",lastname);
        usr.putString("password",encrypted_password);
        req.putString("action","find");
        req.putString("collection","users");
        req.putObject("matcher",usr);
        return req;
    }
}
