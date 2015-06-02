package com.cai.pres_de_vous.mongod;

import com.cai.pres_de_vous.utils.User;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Yoann Diquélou on 02/06/15.
 * Use Mongo DB to singn-in sign-up and check other things in database
 */
public class DBHelper extends Verticle {
    EventBus eb;
    private static MessageDigest md;

    @Override
    public void start() {
        super.start();

        eb = vertx.eventBus();

        Handler<Message<JsonObject>> dbHandler = new Handler<Message<JsonObject>>() {

            @Override
            public void handle(final Message<JsonObject> request) {
                JsonObject obj = request.body();
                String action = obj.getString("action");
                switch(action){
                   case "SIGN UP":
                       String password;
                       password = cryptWithSHA(obj.getString("password"));
                       if(!password.equals("")) {
                           final User user = new User(obj.getString("firstname"), obj.getString("lastname"), password);
                           container.logger().info(user.registerUserRequest().toString());
                           vertx.eventBus().send("mongodb-persistor", user.findRequest(), new Handler<Message<JsonObject>>() {
                               @Override
                               public void handle(Message<JsonObject> event) {
                                   container.logger().info("message: "+event.body().encodePrettily());
                                   if(event.body().getInteger("number")<1){
                                       vertx.eventBus().send("mongodb-persistor", user.registerUserRequest(), new Handler<Message<JsonObject>>() {
                                           @Override
                                           public void handle(Message<JsonObject> subscribe) {
                                                request.reply(subscribe.body());
                                           }
                                       });
                                   }else{
                                        JsonObject rep = new JsonObject();
                                       rep.putString("result","user already exist");
                                       rep.putValue("code",300);
                                       request.reply(rep);
                                   }
                                   //request.reply(event.body());
                               }
                           });
                       }else{
                           container.logger().info("cannot create user");
                           request.reply("Cannot create user");
                       }
                       break;
                   default:
                       request.reply("No available action");
                }
            }

        };
        eb.registerHandler("DBHelper-auth", dbHandler);
    }

    public static String cryptWithSHA(String pass){
        try {
            md = MessageDigest.getInstance("SHA-256");
            byte[] passBytes = pass.getBytes();
            md.reset();
            byte[] digested = md.digest(passBytes);
            StringBuffer sb = new StringBuffer();
            for(int i=0;i<digested.length;i++){
                sb.append(Integer.toHexString(0xff & digested[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return null;


    }


}
