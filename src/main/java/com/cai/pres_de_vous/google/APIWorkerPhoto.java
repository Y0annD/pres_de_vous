package com.cai.pres_de_vous.google;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.concurrent.ConcurrentMap;

/**
 * APIWorkerPhoto va récupérer l'URL des différentes photos en fonction de la référence Google qui lui est passé. Il fait appel à l'API Google Photos
 * Created by math29 on 07/06/15.
 * @author math29
 * @version 1.0
 * @see APIWorkerReference
 * @see APIWorker
 */
public class APIWorkerPhoto extends Verticle{
    /**
     * Client HTTP qui va permettre faire un appel à l'API Google Photo
     */
    private HttpClient client;

    /**
     * Méthode start de notre API Worker Photo
     */
    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Google.APIWorker");

        EventBus eb = vertx.eventBus();

        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {
            String response = "";

            /**
             * Handler qui va faire l'appel à l'API Google Photo pour récupérer l'URL de nos photos en fonction des références.
             * @param message
             */
            @Override
            public void handle(final Message<JsonObject> message) {

                ConcurrentMap<Integer, String> map = vertx.sharedData().getMap("worker.photo");

                String link = "/maps/api/place/photo?maxwidth=400&photoreference="+message.body().getString("photo_reference")+"&key=AIzaSyB_ZF1jLbtlf019YqtWxk_o4vZ2SxqWixo";
                int number = message.body().getInteger("number");

                System.out.println("link: "+link);
                client = vertx.createHttpClient().setSSL(true).setTrustAll(true).setPort(443).setHost("maps.googleapis.com");

                client.getNow(link, new Handler<HttpClientResponse>() {

                    Buffer body = new Buffer(0);

                    public void handle(HttpClientResponse response) {
                        response.dataHandler(new Handler<Buffer>() {
                            public void handle(Buffer data) {
                                body.appendBuffer(data);
                            }
                        });

                        response.endHandler(new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                                Buffer buff = new Buffer(body.toString());
                                int len = buff.length();
                                String img = buff.getString(168, len-29);
                                map.put(number, img);
                                message.reply();
                            }
                        });
                    }
                });
            }
        };

        eb.registerHandler("google.servicePhoto", apiHandler);
    }
}