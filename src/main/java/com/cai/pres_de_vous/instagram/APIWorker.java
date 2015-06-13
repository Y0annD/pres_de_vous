package com.cai.pres_de_vous.instagram;

import com.cai.pres_de_vous.utils.User;
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
 * Created by crocus on 30/05/15.
 */
public class APIWorker extends Verticle {


    @Override
    public void start() {
        super.start();

        container.logger().info("Deploy Instagram.APIWorker");

        EventBus eb = vertx.eventBus();


        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {
            String response = "";
            @Override
            public void handle(final Message<JsonObject> message) {

                String cookie = message.body().getString("cookie");
                //ConcurrentMap<String, String> map = vertx.sharedData().getMap("storage.user");

                //User user = new User(new JsonObject(map.get(cookie)));
                //1908124175.812f30f.2bf9fef724754d2d840dfe3fea402626
                String link = "/v1/media/search?lat="+message.body().getString("latitude")+"&lng="+message.body().getString("longitude")+"&access_token=1908124175.812f30f.2bf9fef724754d2d840dfe3fea402626";
                container.logger().info("link: "+link);
                HttpClient client = vertx.createHttpClient().setSSL(true).setTrustAll(true).setPort(443).setHost("api.instagram.com");

                String proxyHost = System.getProperty("http.proxyHost", "none");
                Integer proxyPort = Integer.valueOf(System.getProperty("http.proxyPort", "80"));

                if(!"none".equalsIgnoreCase(proxyHost)){
                    container.logger().info("set proxy to "+proxyHost+":"+proxyPort);
                    client.setHost(proxyHost);
                    client.setPort(proxyPort);
                    link = "https://api.instagram.com"+link;
                }
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

                                message.reply(body.toString());
                            }
                        });
                    }
                });
                //client.close();
            }
        };

        eb.registerHandler("instagram.service", apiHandler);
    }
}