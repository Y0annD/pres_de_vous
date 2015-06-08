package com.cai.pres_de_vous.google;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by math on 07/06/15.
 */
public class APIWorker extends Verticle{
    public int counter =0 ;

    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Google.APIWorker");

        final EventBus eb = vertx.eventBus();

        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {
            String response = "";

            @Override
            public void handle(final Message<JsonObject> message) {
                JsonObject point = message.body();
                eb.send("google.serviceRef", point, new Handler<Message<String>>() {
                    @Override
                    public void handle(Message<String> eventBusResponse) {
                        ConcurrentMap<String, String> map = vertx.sharedData().getMap("google.myset");
                        JsonArray obj = new JsonArray(eventBusResponse.body());
                        for(int i=0; i<obj.size(); i++){ //On récupère ici les references des photos une par une
                            JsonObject ref_photo = obj.get(i);
                            //container.logger().info("Nous avons récupéré une référence : "+ref_photo+". Nous allons maintenant recupérer sa photo");
                            eb.send("google.servicePhoto", ref_photo, new Handler<Message<String>>() {
                                @Override
                                public void handle(Message<String> eventBusResponse) {
                                    //ConcurrentMap<String, String> map = vertx.sharedData().getMap("google.myset");
                                    //map.put("some-key", "Blah");
                                    counter++;
                                    //container.logger().info("On incrémente le counter "+map);
                                    //googlePhotos.putString("photo", eventBusResponse.body());
                                    //counter++;
                                    //event.response().end(eventBusResponse.body().toString());
                                }
                            });
                        }
                        //message.reply(googlePhotos);
                    }
                });
                //googlePhotos.putString("photo", "hihi");
                container.logger().info("Le compteur en est a  : "+Integer.toString(counter));
            }
        };

        eb.registerHandler("google.service", apiHandler);
    }
}