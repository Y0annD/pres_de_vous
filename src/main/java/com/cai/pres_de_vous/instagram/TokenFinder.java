package com.cai.pres_de_vous.instagram;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.*;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import javax.net.ssl.SSLContext;
import java.util.Set;

/**
 * Created by crocus on 08/06/15.
 */
public class TokenFinder extends Verticle {

    @Override
    public void start() {
        super.start();

        container.logger().info("Deploy TokenFinder");
        EventBus eb = vertx.eventBus();

        Handler<Message<String>> tokenHandler = new Handler<Message<String>>() {
            @Override
            public void handle(final Message<String> request) {
                HttpClient client = vertx.createHttpClient().setSSL(true).setTrustAll(true).setPort(443).setHost("api.instagram.com");
                container.logger().info("find token");

                HttpClientRequest clientRequest = client.post("/oauth/access_token", new Handler<HttpClientResponse>() {

                    Buffer body = new Buffer(0);

                    public void handle(final HttpClientResponse response) {
                        response.dataHandler(new Handler<Buffer>() {
                            public void handle(Buffer data) {
                                //container.logger().info("data: " + data.toString());
                                body.appendBuffer(data);
                            }
                        });

                        response.endHandler(new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                                container.logger().info(body.toString());
                                request.reply(body.toString());
                            }
                        });
                    }
                });
               Buffer buf = new Buffer("client_id=812f30fbd3144acf843bae2b8d4050ee&");
                buf.appendBuffer(new Buffer("client_secret=01bef22d019446b094672c9eb5f9d8cc&"));
                buf.appendBuffer(new Buffer("grant_type=authorization_code&"));
                buf.appendBuffer(new Buffer("redirect_uri=http://localhost:8081/token/insta&"));
                buf.appendBuffer(new Buffer("code=50e61d07b963413ba630263540be94b1"));
                clientRequest.putHeader("Content-length", "" + buf.length());
                clientRequest.write(buf.toString());
            clientRequest.end();
            }
        };

        eb.registerHandler("instagram.token", tokenHandler);
    }
}
