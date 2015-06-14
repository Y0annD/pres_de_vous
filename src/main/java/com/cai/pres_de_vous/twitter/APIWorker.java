package com.cai.pres_de_vous.twitter;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created by crocus on 14/06/15.
 */
public class APIWorker extends Verticle {



    @Override
    public void start() {
        super.start();

        container.logger().info("Deploy Instagram.APIWorker");

        EventBus eb = vertx.eventBus();

        Handler<Message<JsonObject>> twitterHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
                        .apiKey("dUwKQI88r79uh1QXM28RrtOQ2")
                        .apiSecret("fDvVB8oAqjbqBhfP8va1nRCoqfJohmQ7f6f6xCkTGZn78niAgp")
                        .build();

                String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/search/tweets.json?q=%20&geocode="+event.body().getValue("latitude")+"%2C"+event.body().getValue("longitude")+"%2C10km";

                Verifier verifier = new Verifier("jpU84YotxmD97YUSzeB6hBaOUlwed24bCj8c46EscOD79");
                Token requestToken = new Token("qT-oegAAAAAAgI3nAAABTfILbY0","5hWFfDdB0TJZAWyLXXBtfrRditimEQf2");
                Token accessToken = new Token("817960597-NPcTsLGUVdoxYVBWf5ZotIRXLNfQAYry3o4QTsJB","jpU84YotxmD97YUSzeB6hBaOUlwed24bCj8c46EscOD79");
                //Token accessToken = service.getAccessToken(requestToken, verifier);

                OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);

                service.signRequest(accessToken, request);
                Response response = request.send();
                System.out.println("Got it! Lets see what we found...");
                System.out.println();
                System.out.println(response.getBody());
                event.reply(new JsonObject(response.getBody()));
            }
        };

        eb.registerHandler("twitter.service",twitterHandler);
    }
}
