package com.cai.pres_de_vous.google;

import com.englishtown.promises.Promise;
import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertx.promises.impl.DefaultWhenEventBus;
import com.englishtown.vertx.promises.impl.VertxExecutor;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.platform.Verticle;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by crocus on 30/05/15.
 */
public class APIWorkerReference extends Verticle {

    private HttpClient client;
    private int counter;

    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Google.APIWorker");

        EventBus eb = vertx.eventBus();

        VertxExecutor executor = new VertxExecutor(vertx);
        When when = WhenFactory.createFor(() -> executor);
        DefaultWhenEventBus whenEventBus = new DefaultWhenEventBus(eb, when);

        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {

            @Override
            public void handle(final Message<JsonObject> message) {

                counter = 0;
                String lat = message.body().getString("latitude");
                String lng = message.body().getString("longitude");
                Integer perim = message.body().getInteger("perimetre");

                String link = "/maps/api/place/nearbysearch/json?location="+lat+","+lng+"&radius="+perim+"&key=AIzaSyB_ZF1jLbtlf019YqtWxk_o4vZ2SxqWixo";
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
                                JsonObject obj = new JsonObject(body.toString());
                                JsonArray refPhotos = listReferencesPhotos(obj);

                                // Cas où on ne trouve pas de photos dans notre périmètre : On élargie la recherche
                                if(refPhotos.size() == 0){
                                    JsonObject point = new JsonObject();
                                    point.putString("latitude", lat);
                                    point.putString("longitude", lng);
                                    point.putNumber("perimetre", (perim + (500)));
                                    //container.logger().info("On lance un requete imbriquée !!!! ");

                                    List<Promise<Message<JsonObject>>> promises = new ArrayList<>();
                                    promises.add(whenEventBus.sendWithTimeout("google.serviceRef", point, 15000));

                                    //container.logger().info(when.toString());
                                    when.all(promises).then(
                                            replies -> {
                                                // On success
                                                message.reply();
                                                return null;
                                            },
                                            t -> {
                                                // On fail
                                                //container.logger().info("error !!!!!!!!!!!!!!!!!!!!!!!!!");
                                                ConcurrentMap<Integer, Integer> error = vertx.sharedData().getMap("worker.error");
                                                error.put(0, 1);
                                                message.reply();
                                                return null;
                                            });
                                }else{
                                    //container.logger().info("On lance une requete Worker Reference !!!! ");
                                    java.util.List<Promise<Message<JsonObject>>> promises = new ArrayList<>();
                                    for(int i=0; i<refPhotos.size(); i++){ //On récupère ici les references des photos une par une
                                        JsonObject ref_photo = refPhotos.get(i);
                                        ref_photo.putNumber("number", counter);
                                        //container.logger().info("Ici on envoie la référence suivante : "+ref_photo.toString());
                                        //container.logger().info("Nous avons récupéré une référence : "+ref_photo+". Nous allons maintenant recupérer sa photo");

                                        promises.add(whenEventBus.sendWithTimeout("google.servicePhoto", ref_photo, 15000));

                                        //container.logger().info(when.toString());
                                        when.all(promises).then(
                                                replies -> {
                                                    // On success
                                                    //container.logger().info("success in converting reference to photo "+counter+" !!!!!!!!!!!!!!!!!!!!!!!!!");
                                                    message.reply();
                                                    return null;
                                                },
                                                t -> {
                                                    // On fail
                                                    //container.logger().info("error in converting reference to photo !!!!!!!!!!!!!!!!!!!!!!!!!");
                                                    ConcurrentMap<Integer, Integer> error = vertx.sharedData().getMap("worker.error");
                                                    error.put(0, 1);
                                                    message.reply();
                                                    return null;
                                                });

                                        counter++;
                                    }
                                }
                                //container.logger().info("On finit notre bordel !!!!!!!");

                                //map.put(0, refPhotos);
                                //container.logger().info("On a le message suivant a te filer : "+refPhotos.toString());
                            }
                        });
                    }
                });
            }
        };

        eb.registerHandler("google.serviceRef", apiHandler);
    }

    public JsonArray listReferencesPhotos(JsonObject obj){
        int counter = 0;
        ConcurrentMap<Integer, String> refs = vertx.sharedData().getMap("worker.references");
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
                        JsonObject imgDes = formatResponse(result, photo);
                        refs.put(counter, imgDes.encode());
                        counter++;
                    }
                }
            }
        }
        //container.logger().info(listeReferences);
        return listeReferences;
    }

    public JsonObject formatResponse(JsonObject obj, JsonObject photo){
        JsonObject returnPhoto = new JsonObject();
        //container.logger().info("On recoit ceci comme obj : "+obj.toString());
        //container.logger().info("On recoit ceci comme photo : " + photo.toString());

        // SOURCE
        returnPhoto.putString("source", "google");

        // LOCATION TRANSLATION
        JsonObject objLocation = new JsonObject();
        objLocation.putValue("latitude", obj.getObject("geometry").getObject("location").getValue("lat"));
        objLocation.putValue("longitude", obj.getObject("geometry").getObject("location").getValue("lng"));
        returnPhoto.putObject("location", objLocation);

        // LINK
        returnPhoto.putString("link", "");

        // IMAGE
        JsonObject img = new JsonObject();
        img.putValue("width", photo.getValue("width"));
        img.putValue("height", photo.getValue("height"));
        returnPhoto.putObject("image", img);

        // AUTHOR
        JsonObject author = new JsonObject();
        author.putString("username", obj.getString("name"));
        returnPhoto.putObject("author", author);

        //container.logger().info("On renvoie ceci : "+returnPhoto.toString());
        //String reference = photo.getString("photo_reference");  // Pour chaque photo on récupère sa référence

        return returnPhoto;
    }
}