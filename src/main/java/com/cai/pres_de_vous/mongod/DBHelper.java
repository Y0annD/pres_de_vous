package com.cai.pres_de_vous.mongod;

import com.cai.pres_de_vous.utils.User;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Created by Yoann Diquélou on 02/06/15.
 * Use Mongo DB to singn-in sign-up and check other things in database
 */
public class DBHelper extends Verticle {
    EventBus eb;
    private static MessageDigest md;
    private static String DB_PATH = "mongodb-persistor";

    @Override
    public void start() {
        super.start();

        eb = vertx.eventBus();

        Handler<Message<JsonObject>> dbHandler = new Handler<Message<JsonObject>>() {

            @Override
            public void handle(final Message<JsonObject> request) {
                JsonObject obj = request.body();
                final User user;
                String action = obj.getString("action");
                String password;

                switch(action){
                    // add a new user in the database
                   case "SIGN_UP":
                       // crypt password with SHA256
                       password = cryptWithSHA(obj.getString("password"));
                       if(!password.equals("")) {
                           // create the user Object
                           user = new User(obj.getString("userName"), password,obj.getString("email"));
                           container.logger().info(user.registerUserRequest().toString());
                           // check if user exist or not
                           eb.send(DB_PATH, user.findRequest(), new Handler<Message<JsonObject>>() {
                               @Override
                               public void handle(Message<JsonObject> event) {
                                   container.logger().info("message: " + event.body().encodePrettily());
                                   // if no users are found, we can create him
                                   if (event.body().getInteger("number") < 1) {
                                       vertx.eventBus().send(DB_PATH, user.registerUserRequest(), new Handler<Message<JsonObject>>() {
                                           @Override
                                           public void handle(Message<JsonObject> subscribe) {
                                               JsonObject obj = new JsonObject();
                                               obj.putString("cookie","sessionid="+user.getToken()+";Path=/; Domain=localhost");
                                               obj.putString("move","https://api.instagram.com/oauth/authorize/?client_id=812f30fbd3144acf843bae2b8d4050ee&redirect_uri=http://localhost:8081/token/insta&response_type=code");

                                               request.reply(obj);
                                           }
                                       });
                                   } else {
                                       JsonObject rep = new JsonObject();
                                       rep.putString("result", "user already exist");
                                       rep.putValue("code", 300);
                                       request.reply(rep);
                                   }
                                   //request.reply(event.body());
                               }
                           });
                       }else{
                           // password is incorect
                           JsonObject rep = new JsonObject();
                           rep.putString("result","incorrect password");
                           rep.putValue("code",403);
                           request.reply(rep);
                           container.logger().info("cannot create user");
                       }
                       break;
                    case "SIGN_IN":
                        // crypt password with SHA256
                        password = cryptWithSHA(obj.getString("password"));
                        // create the user Object
                        user = new User(obj.getString("userName"), password,obj.getString("email"));
                        eb.send(DB_PATH, user.sign_in(), new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> event) {
                                JsonObject reponse = event.body();
                                // credentials are valid
                                if(reponse.getInteger("number")==1){
                                    JsonObject userTest = event.body().getArray("results").get(0);
                                    container.logger().info(event.body().getFieldNames().toString());
                                    JsonObject obj = new JsonObject();
                                    if(!userTest.getString("token").equals(null)){
                                        obj.putString("cookie","sessionid="+userTest.getString("token")+";Path=/; Domain=localhost");
                                        obj.putValue("code",200);
                                    }else{
                                        obj.putValue("code",400);
                                    }

                                    request.reply(obj);

                                }else{
                                    request.reply(event.body());
                                }

                            }
                        });
                        break;
                    case "FIND":
                        String token = obj.getString("token");
                        container.logger().info("token:"+token+"|");
                        user = new User();
                        JsonObject req = user.findUserByToken(token);
                        container.logger().info(req.toString());
                        eb.send(DB_PATH, req, new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> event) {
                                request.reply(event.body());
                            }
                        });
                        break;
                   default:
                       // password is incorect
                       JsonObject rep = new JsonObject();
                       rep.putString("result","No such action");
                       rep.putValue("code",401);
                       request.reply(rep);
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
