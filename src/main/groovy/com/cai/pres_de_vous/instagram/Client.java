package com.cai.pres_de_vous.instagram;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.*;
import org.vertx.java.platform.Verticle;

import javax.net.ssl.SSLContext;
import java.util.Set;

/**
 * Created by yoann Diquélou on 29/05/15.
 */
public class Client extends Verticle{

    @Override
    public void start() {
        HttpClient instaClient = vertx.createHttpClient();

        instaClient.getNow("api.enib.net", new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse event) {
                event.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        System.out.println("Response (" + buffer.length() + "): ");
                        System.out.println(buffer.getString(0, buffer.length()));
                    }
                });
            }
        });
    }
}
