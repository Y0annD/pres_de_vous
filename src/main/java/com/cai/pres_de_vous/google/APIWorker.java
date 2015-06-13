package com.cai.pres_de_vous.google;

import com.englishtown.promises.Promise;
import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertx.promises.impl.DefaultWhenEventBus;
import com.englishtown.vertx.promises.impl.VertxExecutor;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by math on 07/06/15.
 */
public class APIWorker extends Verticle{
    public int counter =0 ;
    private JsonObject response;

    @Override
    public void start() {
        super.start();
        ConcurrentMap<Integer, String> map = vertx.sharedData().getMap("worker.photo");


        VertxExecutor executor = new VertxExecutor(vertx);
        When when = WhenFactory.createFor(() -> executor);
        DefaultWhenEventBus whenEventBus = new DefaultWhenEventBus(vertx.eventBus(), when);
        System.out.println("Deploy Google.APIWorker");

        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {

            @Override
            public void handle(final Message<JsonObject> message) {
                JsonObject point = message.body();
                response = new JsonObject();

                List<Promise<Message<JsonObject>>> promises = new ArrayList<>();
                container.logger().info("Point: "+point.toString());
                promises.add(whenEventBus.sendWithTimeout("google.serviceRef", point, 15000));

                //container.logger().info(when.toString());
                when.all(promises).then(
                        replies -> {
                            // On success
                            //container.logger().info(replies.size());
                    /*        Iterator<Message<JsonObject>> it = replies.iterator();
                            while(it.hasNext()) {
                                Message<JsonObject> m = it.next();
                                container.logger().info("Le reponse est :"+m.body().toString());
                            }*/
                            for(int i = 0; i<map.size(); i++) {
                                response.putString("photo"+i, map.get(i));
                                container.logger().info("Le reponse est : " +map.get(i));

                            }
                            container.logger().info("Yes we did it !!!!!!!!!!!!!!!!!!!!!!!!! LA reponse est :"+response.toString());
                            message.reply(response.toString());
                            return null;
                        },
                        t -> {
                            // On fail
                            container.logger().info("error !!!!!!!!!!!!!!!!!!!!!!!!!");
                            return null;
                        });
            }
        };

        vertx.eventBus().registerHandler("google.service", apiHandler);
    }
}