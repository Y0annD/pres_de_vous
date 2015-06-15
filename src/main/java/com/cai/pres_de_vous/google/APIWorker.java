package com.cai.pres_de_vous.google;

import com.englishtown.promises.Promise;
import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertx.promises.impl.DefaultWhenEventBus;
import com.englishtown.vertx.promises.impl.VertxExecutor;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by math on 07/06/15.
 * package fr.dworkin.sdz.javadoc;
 * Worker permettant de récupérer la liste des photos en fonction des coordonnées qui lui sont passés.
 * Ce Worker utilise deux autres workers ( APIWorkerReference et APIWorkerPhoto )
 *
 * @author math29
 * @version 1.0
 * @see APIWorkerReference
 * @see APIWorkerPhoto
 */

public class APIWorker extends Verticle{

    /**
     * Réponse retour de notre Worker de type JsonObject, c'est le JSON qui contient la liste de nos images provenant
     * de l'API GOOGLE.
     * @see APIWorker#start()
     */
    private JsonObject response;

    /**
     * Méthode start de notre WORKER, fait un reply de notre response
     * @see APIWorker#response
     */
    @Override
    public void start() {
        super.start();
        ConcurrentMap<Integer, String> map = vertx.sharedData().getMap("worker.photo");
        ConcurrentMap<Integer, String> refs = vertx.sharedData().getMap("worker.references");
        ConcurrentMap<Integer, Integer> error = vertx.sharedData().getMap("worker.error");


        VertxExecutor executor = new VertxExecutor(vertx);
        When when = WhenFactory.createFor(() -> executor);
        DefaultWhenEventBus whenEventBus = new DefaultWhenEventBus(vertx.eventBus(), when);
        System.out.println("Deploy Google.APIWorker");

        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {

            /**
             * Ce handler lance l'API google service references avec des promises pour être sûr d'avoir une réponse complète
             * @param message
             */
            @Override
            public void handle(final Message<JsonObject> message) {
                JsonObject point = message.body();
                response = new JsonObject();
                List<Promise<Message<JsonObject>>> promises = new ArrayList<>();
                promises.add(whenEventBus.sendWithTimeout("google.serviceRef", point, 15000));


                when.all(promises).then(
                        replies -> {
                            // On success
                            if(error.get(0) == null){   // Check si nous avons eu une erreur durant l'exécution
                                JsonObject photo;
                                String url;
                                JsonArray results = new JsonArray();
                                // Ajout de l'URL manquante pour chaque image
                                for(int i = 0; i<map.size(); i++) {
                                    photo = new JsonObject(refs.get(i));
                                    url = map.get(i);
                                    photo.getObject("image").putString("url", url);
                                    photo.getObject("author").putString("profile_picture", url);
                                    results.addObject(photo);
                                }
                                // Ajout du code de retour
                                response.putValue("code", 200);
                                response.putArray("results", results);
                                message.reply(response.toString());
                                return null;
                            }else{
                                response.putValue("code", 400);
                                response.putArray("results", new JsonArray());
                                message.reply(response.toString());
                                return null;
                            }
                        },
                        t -> {
                            // On fail
                            response.putValue("code", 400);
                            response.putArray("results", new JsonArray());
                            message.reply(response.toString());
                            return null;
                        });
            }
        };

        vertx.eventBus().registerHandler("google.service", apiHandler);
    }
}