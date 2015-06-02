package com.cai.pres_de_vous.google;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created by crocus on 30/05/15.
 */
public class APIWorkerGoogle extends Verticle {


    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Google.APIWorker");

        EventBus eb = vertx.eventBus();

        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {
            String response = "";
            @Override
            public void handle(final Message<JsonObject> message) {/*
                try {
                    switch (message.body()) {
                        case "beers":
                            response = "Cool";
                            break;

                        //other beers
                        default:
                            response ="Pas cool";
                    }
                }catch(Exception exp) {
                    response = "Huston we have a problem";
                    exp.printStackTrace();
                }
                */
                String link = "/maps/api/place/nearbysearch/json?location="+message.body().getString("latitude")+","+message.body().getString("longitude")+"&radius=500&key=AIzaSyB_ZF1jLbtlf019YqtWxk_o4vZ2SxqWixo";
                System.out.println("link: "+link);
                vertx.createHttpClient().setSSL(true).setTrustAll(true).setPort(443).setHost("maps.googleapis.com").getNow(link, new Handler<HttpClientResponse>() {
                    public void handle(HttpClientResponse response) {
                        response.dataHandler(new Handler<Buffer>() {
                            public void handle(Buffer data) {
                                //System.out.println(data);
                                JsonObject rep = new JsonObject(data.toString());

                                message.reply(rep.getArray("data").toString());
                            }
                        });
                    }
                });
                // Now reply to it
                //message.reply(response);
            }
        };

        eb.registerHandler("google.service", apiHandler);
    }
}
