package com.cai.pres_de_vous.twitter;

import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertx.promises.impl.DefaultWhenEventBus;
import com.englishtown.vertx.promises.impl.VertxExecutor;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Scanner;

/**
 * Created by crocus on 13/06/15.
 */
public class TokenFinder extends Verticle {

    private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/search/tweets.json?q=%20&geocode=48.3341578%2C-4.418471%2C10km";

    @Override
    public void start() {
        super.start();
        EventBus eb = vertx.eventBus();

        VertxExecutor executor = new VertxExecutor(vertx);
        When when = WhenFactory.createFor(() -> executor);
        DefaultWhenEventBus whenEventBus = new DefaultWhenEventBus(vertx.eventBus(), when);
        container.logger().info("Deploy Twitter.Token");

        Handler<Message<JsonObject>> tokenHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
                        .apiKey("dUwKQI88r79uh1QXM28RrtOQ2")
                        .apiSecret("fDvVB8oAqjbqBhfP8va1nRCoqfJohmQ7f6f6xCkTGZn78niAgp")
                        .callback("http://127.0.0.1:8081/twitter/ok")
                        .build();

                System.out.println("=== Twitter's OAuth Workflow ===");
                System.out.println();

                // Obtain the Request Token
                System.out.println("Fetching the Request Token...");
                Token requestToken = service.getRequestToken();
                System.out.println("Got the Request Token!");
                System.out.println();

                String code = event.body().getString("code");
                container.logger().info("code = "+code);
                System.out.println("Now go and authorize Scribe here:");
                String verif = service.getAuthorizationUrl(requestToken);
                if(code.equals("")) {
                    //event.reply("running");
                    JsonObject obj = new JsonObject();
                    obj.putString("redirect",verif);
                    event.reply(obj);
                }else {
                    event.reply(new JsonObject("{ \"verification\":\"ok\"}"));
                    System.out.println("And paste the verifier here");
                    System.out.print(">>");
                    System.out.print(event.body().getString("oauth_verifier"));
                    Verifier verifier = new Verifier(event.body().getString("oauth_verifier"));
                    System.out.println();
                    container.logger().info("");
                    // Trade the Request Token and Verfier for the Access Token
                    // System.out.println("Trading the Request Token for an Access Token...");
                    requestToken = new Token(event.body().getString("oauth_token"),event.body().getString("oauth_verifier"));
                    Token accessToken = service.getAccessToken(requestToken, verifier);
                    System.out.println("Got the Access Token!");
                    System.out.println("(if you're curious, it looks like this: " + accessToken + " )");
                    System.out.println();

                    // Now let's go and ask for a protected resource!
                System.out.println("Now we're going to access a protected resource...");
                OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
                service.signRequest(accessToken, request);
                Response response = request.send();
                System.out.println("Got it! Lets see what we found...");
                System.out.println();
                System.out.println(response.getBody());

                    System.out.println();
                    System.out.println("That's it man! Go and build something awesome with Scribe! :)");
                }
            }
        };

        eb.registerHandler("twitter.token",tokenHandler);
    }

}
