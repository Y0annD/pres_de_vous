package com.cai.pres_de_vous;

import org.vertx.java.platform.Verticle;

/**
 * Created by crocus on 30/05/15.
 */
public class EntryPoint extends Verticle {

    @Override
    public void start() {
        super.start();

        container.deployWorkerVerticle("com.cai.pres_de_vous.instagram.APIWorker",1);

        container.deployVerticle("com.cai.pres_de_vous.Server", container.config());
    }


}
