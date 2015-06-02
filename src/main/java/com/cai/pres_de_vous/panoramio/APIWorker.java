package com.cai.pres_de_vous.panoramio;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created by crocus on 30/05/15.
 */
public class APIWorker extends Verticle {


    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Panoramio.APIWorker");

        EventBus eb = vertx.eventBus();

        Handler<Message<JsonObject>> apiHandler = new Handler<Message<JsonObject>>() {
            String response = "";
            String stringMinx, stringMiny, stringMaxx, stringMaxy;
            int minx, miny, maxx, maxy;
            @Override
            public void handle(final Message<JsonObject> message) {/*
                try {
                    switch (message.body()) {
                        case "beers":
                            response = "Cool";
                            break;

                        //other beers
                        default:
                            response ="Pas cool";
                    }
                }catch(Exception exp) {
                    response = "Huston we have a problem";
                    exp.printStackTrace();
                }
                */
                stringMinx = message.body().getString("longitude");
                stringMiny = message.body().getString("latitude");
                stringMaxx = message.body().getString("longitude");
                stringMaxy = message.body().getString("latitude");

                System.out.println("Début calcul Longitude / Latitude Panoramio : Long = "+message.body().getString("longitude")+" / Lat = "+message.body().getString("latitude"));
                System.out.println("Deploy Panoramio.APIWorker");
                try{
                    minx = Integer.parseInt(message.body().getString("longitude"))- 10;
                    maxx = Integer.parseInt(message.body().getString("longitude"))+ 10;
                    miny = Integer.parseInt(message.body().getString("latitude")) -10;
                    maxy = Integer.parseInt(message.body().getString("latitude")) +10;

                    stringMinx = Integer.toString(minx);
                    stringMiny = Integer.toString(miny);
                    stringMaxx = Integer.toString(maxx);
                    stringMaxy = Integer.toString(maxy);
                }catch(NumberFormatException ex){ // handle your exception
                    System.out.println("Nous avons une erreur lors du calcul de longitude / latitude !");
                }

                String link = "http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to=20&minx="+stringMinx+"&miny="+stringMiny+"&maxx="+stringMaxx+"&maxy="+stringMaxy+"&size=medium&mapfilter=true";
                System.out.println("link: "+link);
                vertx.createHttpClient().setTrustAll(true).getNow(link, new Handler<HttpClientResponse>() {
                    public void handle(HttpClientResponse response) {
                        response.dataHandler(new Handler<Buffer>() {
                            public void handle(Buffer data) {
                                System.out.println(data);
                                JsonObject rep = new JsonObject(data.toString());

                                message.reply(rep.getArray("data").toString());
                            }
                        });
                    }
                });
                // Now reply to it
                //message.reply(response);
            }
        };

        eb.registerHandler("panoramio.service", apiHandler);
    }
}
