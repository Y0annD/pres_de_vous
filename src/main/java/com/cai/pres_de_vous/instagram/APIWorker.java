package com.cai.pres_de_vous.instagram;

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
public class APIWorker extends Verticle {


    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Instagram.APIWorker");

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
                String link = "/v1/media/search?lat="+message.body().getString("latitude")+"&lng="+message.body().getString("longitude")+"&access_token=1908124175.812f30f.2bf9fef724754d2d840dfe3fea402626";
                link = "/v1/media/search?lat=48.334156&lng=-4.418471&access_token=1908124175.812f30f.2bf9fef724754d2d840dfe3fea402626";
                System.out.println("link: "+link);
                vertx.createHttpClient().setSSL(true).setTrustAll(true).setPort(443).setHost("api.instagram.com").getNow(link, new Handler<HttpClientResponse>() {

                    Buffer body = new Buffer(0);
                    public void handle(HttpClientResponse response) {
                        response.dataHandler(new Handler<Buffer>() {
                            public void handle(Buffer data) {
                                //System.out.println(data);
                                //JsonObject rep = new JsonObject(data.toString());
                                body.appendBuffer(data);
                                //message.reply(data.toString());
                            }
                        });

                        response.endHandler(new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                                message.reply(body.toString());
                            }
                        });
                    }
                });
                // Now reply to it
                //message.reply(response);
            }
        };

        eb.registerHandler("instagram.service", apiHandler);
    }
}