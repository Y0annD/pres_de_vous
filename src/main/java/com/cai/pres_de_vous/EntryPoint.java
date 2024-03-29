package com.cai.pres_de_vous;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Start all verticles, workers and modules
 */
public class EntryPoint extends Verticle {

    @Override
    public void start() {
        super.start();
        JsonObject appConfig = container.config();

        container.deployWorkerVerticle("com.cai.pres_de_vous.google.APIWorker",1);
        container.deployWorkerVerticle("com.cai.pres_de_vous.google.APIWorkerPhoto",1);
        container.deployWorkerVerticle("com.cai.pres_de_vous.google.APIWorkerReference",1);


        container.deployWorkerVerticle("com.cai.pres_de_vous.instagram.APIWorker",1);
        container.deployWorkerVerticle("com.cai.pres_de_vous.instagram.TokenFinder");

        container.deployWorkerVerticle("com.cai.pres_de_vous.twitter.TokenFinder");
        container.deployWorkerVerticle("com.cai.pres_de_vous.twitter.APIWorker");

        container.deployWorkerVerticle("com.cai.pres_de_vous.mongod.DBHelper");


        // deploy mongoDB link
        container.deployModule("io.vertx~mod-mongo-persistor~2.1.1",appConfig.getObject("mongo-persistor"));

        // deploy server
        container.deployVerticle("com.cai.pres_de_vous.Server", container.config());


    }


}
