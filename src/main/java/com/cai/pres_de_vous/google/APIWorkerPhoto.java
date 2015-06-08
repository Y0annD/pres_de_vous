package com.cai.pres_de_vous.google;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created by math on 07/06/15.
 */
public class APIWorkerPhoto extends Verticle{
    private HttpClient client;

    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Google.APIWorker");

        EventBus eb = vertx.eventBus();

        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {
            String response = "";

            @Override
            public void handle(final Message<JsonObject> message) {
                container.logger().info("La référence qui arrive dans le APIWorkerPhoto : "+message.body().getString("photo_reference"));
                String link = "/maps/api/place/photo?maxwidth=400&photoreference="+message.body().getString("photo_reference")+"&key=AIzaSyB_ZF1jLbtlf019YqtWxk_o4vZ2SxqWixo";
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
                                container.logger().info("On traite une fois la photo");
                                JsonObject obj = new JsonObject(body.toString());
                                //container.logger().info("on récupère le body HTML suivant : "+obj.getArray("HTML"));
                                //container.logger().info("On a récupéré le lien de l'image : "+body.toString());
                                message.reply(body.toString());
                            }
                        });
                    }
                });
            }
        };

        eb.registerHandler("google.servicePhoto", apiHandler);
    }

    public JsonArray listReferencesPhotos(JsonObject obj){
        JsonArray listeReferences = new JsonArray();
        JsonArray results = obj.getArray("results");    // On récupère la liste des lieux autours de nous
        for(int i=0; i < results.size(); i++){
            JsonObject result = results.get(i);
            JsonArray photos = result.getArray("photos");   // Pour chaque lieu on récupère la liste des photos associées si elles exitent
            if(photos != null){
                for(int j=0; j < photos.size(); j++){
                    JsonObject photo = photos.get(j);
                    String reference = photo.getString("photo_reference");  // Pour chaque photo on récupère sa référence
                    if(reference != null){
                        listeReferences.addObject(photo);
                        // container.logger().info(reference);
                    }
                }
            }
        }
        //container.logger().info(listeReferences);
        return listeReferences;
    }
}




/*
String reference = photo.getString("photo_reference");
String link = "/maps/api/place/photo?maxwidth=400&photoreference="+reference+"&key=AIzaSyB_ZF1jLbtlf019YqtWxk_o4vZ2SxqWixo";
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
        container.logger().info(body.toString());
        JsonObject obj = new JsonObject(body.toString());
        jSonResponse.putObject("photo"+Integer.toString(counter),obj);
        counter++;
        }
        });
        }
        });*/