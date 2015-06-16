package com.cai.pres_de_vous.twitter;

import com.cai.pres_de_vous.Credentials;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Created by crocus on 14/06/15.
 */
public class APIWorker extends Verticle {



    @Override
    public void start() {
        super.start();

        container.logger().info("Deploy Instagram.APIWorker");

        EventBus eb = vertx.eventBus();

        if(Credentials.proxy_enable) {
            final String authUser = Credentials.username;
            final String authPassword = Credentials.password;
            Authenticator.setDefault(
                    new Authenticator() {
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    authUser, authPassword.toCharArray());
                        }
                    }
            );
            System.setProperty("http.proxyHost", Credentials.proxyHost);
            System.setProperty("http.proxyPort", "" + Credentials.proxyPort);
            System.setProperty("http.proxyUser", authUser);
            System.setProperty("http.proxyPassword", authPassword);
            System.setProperty("https.proxyHost", Credentials.proxyHost);
            System.setProperty("https.proxyPort", "" + Credentials.proxyPort);
            System.setProperty("https.proxyUser", authUser);
            System.setProperty("https.proxyPassword", authPassword);
        }
        Handler<Message<JsonObject>> twitterHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {


                OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
                        .apiKey("dUwKQI88r79uh1QXM28RrtOQ2")
                        .apiSecret("fDvVB8oAqjbqBhfP8va1nRCoqfJohmQ7f6f6xCkTGZn78niAgp")
                        .build();


                String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/search/tweets.json?q=%20&geocode="+event.body().getValue("latitude")+"%2C"+event.body().getValue("longitude")+"%2C5km";

                Verifier verifier = new Verifier("jpU84YotxmD97YUSzeB6hBaOUlwed24bCj8c46EscOD79");
                Token requestToken = new Token("qT-oegAAAAAAgI3nAAABTfILbY0","5hWFfDdB0TJZAWyLXXBtfrRditimEQf2");
                Token accessToken = new Token("817960597-NPcTsLGUVdoxYVBWf5ZotIRXLNfQAYry3o4QTsJB","jpU84YotxmD97YUSzeB6hBaOUlwed24bCj8c46EscOD79");
                //Token accessToken = service.getAccessToken(requestToken, verifier);

                OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);

                //request.addHeader("Proxy-Authorisation","Basic eTFkaXF1ZWw6Rm56KjUwNDZ0Zj8=");

                service.signRequest(accessToken, request);

                Response response = request.send();


                /**
                 * Traitement de la réponse
                 */
                JsonObject twitterResponse = new JsonObject(response.getBody());
                JsonArray tweetsArray = twitterResponse.getArray("statuses");
                int arraySize = tweetsArray.size();
                JsonObject resp = new JsonObject();
                if(arraySize>0) {
                    resp.putValue("code", 200);
                    JsonArray respArray = new JsonArray();
                    for (int i = 0; i < arraySize; i++) {
                        JsonObject obj = new JsonObject();
                        JsonObject tweet = tweetsArray.get(i);
                        obj.putString("source","twitter");
                        if(tweet.getObject("retweeted_status")!=null)tweet = tweet.getObject("retweeted_status");
                        JsonArray locArray = tweet.getObject("geo").getArray("coordinates");
                        JsonObject location = new JsonObject("{\"latitude\":"+locArray.get(0)+",\"longitude\":"+locArray.get(1)+"}");
                        obj.putObject("location",location);


                        JsonObject tweetAuthor = tweet.getObject("user");

                        obj.putString("link","https://twitter.com/"+tweetAuthor.getString("screen_name")+"/status/"+tweet.getString("id_str"));
                        obj.putString("text",tweet.getString("text"));

                        obj.putObject("author",new JsonObject("{\"username\": \""+tweetAuthor.getString("screen_name")+"\", \"profile_picture\": \""+tweetAuthor.getString("profile_image_url")+"\"}"));

                        //obj.putObject("location",);
                        respArray.add(obj);

                    }
                    resp.putArray("results",respArray);
                }else{
                    resp.putValue("code", 400);
                }
                event.reply(resp);
            }
        };

        eb.registerHandler("twitter.service",twitterHandler);
    }
}
