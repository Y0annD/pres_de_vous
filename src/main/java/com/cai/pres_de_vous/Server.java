package com.cai.pres_de_vous;


import com.cai.pres_de_vous.utils.GeoPoint;
import com.cai.pres_de_vous.utils.User;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.SharedData;
import org.vertx.java.platform.Verticle;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Server which serve static files and routes
 **/
public class Server extends Verticle {


    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Server");

        final EventBus eb = vertx.eventBus();
        eb.setDefaultReplyTimeout(25000);

        RouteMatcher routeMatcher = new RouteMatcher();

        routeMatcher.get("/insta/:lat/:lng", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest event) {

                getUser(event);
                String lat = event.params().get("lat");
                String lng = event.params().get("lng");
                GeoPoint point = new GeoPoint(Float.parseFloat(lat),Float.parseFloat(lng));
                if(point.isValid()) {
                    eb.send("instagram.service", point.toJSON(), new Handler<Message<String>>() {
                        @Override
                        public void handle(Message<String> eventBusResponse) {
                            JsonObject ob = new JsonObject(eventBusResponse.body().toString());
                            event.response().end(ob.getArray("data").toString());
                        }
                    });
                    /*eb.send("instagram.service", "beers", new Handler<Message<String>>() {
                        public void handle(Message<String> eventBusResponse) {
                            //System.out.println("Yeah the response is " + eventBusResponse.body());
                            event.response().end(eventBusResponse.body());
                        }
                    });*/
                }else{
                    event.response().end("Invalid position");
                }

            }
        });

        routeMatcher.get("/google/:lat/:lng", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest event) {

                String lat = event.params().get("lat");
                String lng = event.params().get("lng");
                GeoPoint point = new GeoPoint(Float.parseFloat(lat),Float.parseFloat(lng));
                if(point.isValid()) {
                    eb.send("google.service", point.toJSON(), new Handler<Message<String>>() {
                        @Override
                        public void handle(Message<String> eventBusResponse) {
                            event.response().end(eventBusResponse.body());
                        }
                    });
                }else{
                    event.response().end("Invalid position");
                }

            }
        });

        routeMatcher.get("/test/", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest event) {
                //container.logger().info(event.params().size());
                vertx.eventBus().setDefaultReplyTimeout(25000).send("instagram.token", "", new Handler<Message<String>>() {
                    @Override
                    public void handle(Message<String> event) {
                        container.logger().info(event.body().toString());
                    }
                });
                event.response().end("Reçu 5/5 houston");
            }
        });

        routeMatcher.get("/token/:site", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest event) {
                container.logger().info(event.params().get("site"));
                container.logger().info(event.uri().toString());
                container.logger().info(event.params().get("code"));
                event.response().end("token bidon");
            }
        });

        routeMatcher.post("/mongo/SIGN_UP", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                req.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        JsonObject usr = new JsonObject();
                        Map map = getQueryMap(buffer.toString());
                        if(map.get("name")!=null && map.get("password")!=null && map.get("email")!=null){
                            usr.putString("userName",map.get("name").toString());
                            usr.putString("password",map.get("password").toString());
                            usr.putString("email",map.get("email").toString());
                            usr.putString("action","SIGN_UP");
                            eb.send("DBHelper-auth", usr, new Handler<Message<JsonObject>>() {
                                @Override
                                public void handle(Message<JsonObject> event) {
                                    JsonObject obj = event.body();
                                    if(obj.getString("cookie")!=null){
                                        //String cookie = req.headers().get("Cookie"); //La valeur contenue dans cookie
                                        req.response().headers().set("Set-Cookie", obj.getString("cookie"));
                                    }
                                    if(obj.getString("move")!=null) {
                                        req.response().setStatusCode(301); // or SC_FOUND
                                        req.response().putHeader("Location", obj.getString("move"));

                                    }
                                    req.response().end(event.body().encodePrettily());
                                }
                            });
                        }else{

                        }
                    }
                });

            }
        });

        routeMatcher.get("/mongo/:action", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                container.logger().info("in mongo action");
                final JsonObject usr = new JsonObject();
                String action = req.params().get("action");
                //usr.putString("action",req.params().get("action"));
                if(action.equals("SIGN_UP")){
                    req.bodyHandler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {

                        }
                    });
                }
                //usr.putString("firstname","Yoann");
                //usr.putString("lastname","Diquélou");
                //usr.putString("password","bidon");


            }
        });

        routeMatcher.noMatch(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {
                String file = "";
                if (req.path().equals("/")) {
                    file = "index.html";
                } else if (!req.path().contains("..")) {
                    file = req.path();
                }
                req.response().sendFile("webroot/" + file);
            }
        });

        vertx.createHttpServer().requestHandler(routeMatcher).listen(8081);
    }

    /**
     * Get user id session stored in cookie
     * @param event: user request
     * @return String: cookie if exist, null otherwise
     */
    private String getTokenCookie(HttpServerRequest event){
        String cookie = event.headers().get("Cookie"); //La valeur contenue dans cookie
        if(cookie!=null) {
            int substr = cookie.indexOf("=");
            if (substr != -1) {
                return cookie.substring(substr + 1);
            }
        }
        return null;
    }

    private void getUser(HttpServerRequest request){
        final String cookie = getTokenCookie(request);
        if(cookie!=null){

            vertx.eventBus().send("DBHelper-auth", new JsonObject("{\"action\":\"FIND\",\"token\":\"" + cookie + "\"}"), new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> authTest) {
                    if (authTest.body().getInteger("number") == 1) {
                        JsonObject obj = authTest.body().getArray("results").get(0);
                        User user = new User(obj);
                        container.logger().info(user.toJSON().toString());
                        ConcurrentMap<String, String> map = vertx.sharedData().getMap("storage.user");

                        map.put(cookie, user.toJSON().toString());
                    }

                }
            });
        }
    }

    public static Map<String, String> getQueryMap(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = "";

            try {
                value = URLDecoder.decode(param.split("=")[1], "UTF-8");

            } catch (Exception e) {
                System.out.println("wtf exception: " + e.getMessage());
            }


            map.put(name, value);
        }

        return map;
    }
}
