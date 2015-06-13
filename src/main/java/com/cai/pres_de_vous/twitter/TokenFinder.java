package com.cai.pres_de_vous.twitter;

import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertx.promises.impl.DefaultWhenEventBus;
import com.englishtown.vertx.promises.impl.VertxExecutor;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.oauth.OAuthService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

/**
 * Created by crocus on 13/06/15.
 */
public class TokenFinder extends Verticle {



    @Override
    public void start() {
        super.start();
        EventBus eb = vertx.eventBus();

        VertxExecutor executor = new VertxExecutor(vertx);
        When when = WhenFactory.createFor(() -> executor);
        DefaultWhenEventBus whenEventBus = new DefaultWhenEventBus(vertx.eventBus(), when);
        container.logger().info("Deploy Twitter.Token");

        Handler<Message<String>> tokenHandler = new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> event) {
                OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
                        .apiKey("dUwKQI88r79uh1QXM28RrtOQ2")
                        .apiSecret("fDvVBoAqjbqBHfP8va1nRCoqfJohmQ7f6f6xCkTGZ")
                        .build();
            }
        };

        eb.registerHandler("twitter.token",tokenHandler);
    }

}
