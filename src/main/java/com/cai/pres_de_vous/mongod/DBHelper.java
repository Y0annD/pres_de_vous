package com.cai.pres_de_vous.mongod;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created by Yoann Diquélou on 02/06/15.
 * Use Mongo DB to singn-in sign-up and check other things in database
 */
public class DBHelper extends Verticle {
    EventBus eb;

    @Override
    public void start() {
        super.start();

        eb = vertx.eventBus();

        Handler<Message<JsonObject>> dbHandler = new Handler<Message<JsonObject>>() {

            @Override
            public void handle(final Message<JsonObject> request) {
                // we send the response from the mongo query back to the client.
                // first create the query
                JsonObject matcher = new JsonObject().putString("state", "AL");
                JsonObject json = new JsonObject().putString("collection", "zips")
                        .putString("action", "find")
                        .putObject("matcher", matcher);



            }
        };
    }
}
