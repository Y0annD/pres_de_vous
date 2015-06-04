package com.cai.pres_de_vous;


import com.cai.pres_de_vous.utils.GeoPoint;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Server which serve static files and routes
 **/
public class Server extends Verticle {


    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Server");

        final EventBus eb = vertx.eventBus();
        eb.setDefaultReplyTimeout(5000);

        RouteMatcher routeMatcher = new RouteMatcher();

        routeMatcher.get("/insta/:lat/:lng", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest event) {

                String lat = event.params().get("lat");
                String lng = event.params().get("lng");
                GeoPoint point = new GeoPoint(Float.parseFloat(lat),Float.parseFloat(lng));
                if(point.isValid()) {
                    eb.send("instagram.service", point.toJSON(), new Handler<Message<String>>() {
                        @Override
                        public void handle(Message<String> eventBusResponse) {
                            event.response().end(eventBusResponse.body());
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

        routeMatcher.get("/mongo/:action", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                JsonObject usr = new JsonObject();
                container.logger().info(req.params().get("action"));
                usr.putString("action",req.params().get("action"));
                usr.putString("firstname","Yoann");
                usr.putString("lastname","Diquélou");
                usr.putString("password","bidon");
                eb.send("DBHelper-auth", usr, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> event) {
                        req.response().end(event.body().encodePrettily());
                    }
                });
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
}
