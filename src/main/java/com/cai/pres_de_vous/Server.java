package com.cai.pres_de_vous;


import com.cai.pres_de_vous.utils.GeoPoint;
import com.cai.pres_de_vous.utils.User;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Server which serve static files and routes
 **/
public class Server extends Verticle {


    @Override
    public void start() {
        super.start();

        System.out.println("Deploy Server");

        final EventBus eb = vertx.eventBus();
        eb.setDefaultReplyTimeout(25000);

        RouteMatcher routeMatcher = new RouteMatcher();

        routeMatcher.get("/insta/:lat/:lng", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest clientRequest) {

                String cookie = clientRequest.headers().get("Cookie");
                String lat = clientRequest.params().get("lat");
                String lng = clientRequest.params().get("lng");
                JsonObject usr = new JsonObject();
                container.logger().info("cookie: "+cookie);
                if(cookie!=null)
                    usr.putString("token",cookie.substring(cookie.indexOf("=")+1));
                usr.putString("action","FIND");
                final GeoPoint point = new GeoPoint(Float.parseFloat(lat),Float.parseFloat(lng));
                if(point.isValid()) {
                    eb.send("DBHelper-auth", usr, new Handler<Message<JsonObject>>() {
                       @Override

                        public void handle(Message<JsonObject> event) {
                            container.logger().info(event.body());
                            if(event.body().getInteger("number")==1){
                                point.setInstaToken(((JsonObject)event.body().getArray("results").get(0)).getString("insta_key"));
                                eb.send("instagram.service", point.toJSON(), new Handler<Message<JsonObject>>() {
                                    @Override
                                    public void handle(Message<JsonObject> eventBusResponse) {
                                        //JsonObject ob = new JsonObject(eventBusResponse.body().toString());
                                        //clientRequest.response().end(ob.getArray("data").toString());
                                        clientRequest.response().end(eventBusResponse.body().encodePrettily());
                                    }
                                });
                            }else{
                                clientRequest.response().end("Tu n'est pas autorisé jeune padawan");
                            }
                        }
                    });



                }else{
                    clientRequest.response().end("Invalid position");
                }

            }
        });

        routeMatcher.get("/google/:lat/:lng", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest event) {

                String lat = event.params().get("lat");
                String lng = event.params().get("lng");
                GeoPoint point = new GeoPoint(Float.parseFloat(lat),Float.parseFloat(lng));
                if(point.isValid()) {
                    eb.send("google.service", point.toJSON(), new Handler<Message<String>>() {
                        @Override
                        public void handle(Message<String> eventBusResponse) {

                            event.response().end(eventBusResponse.body());
                        }
                    });
                }else{
                    event.response().end("Invalid position");
                }

            }
        });


        /**
         * Retour des token, en tout cas pour l'api instagram
         */
        routeMatcher.get("/token/:site", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest clientRequest) {
                container.logger().info(clientRequest.params().get("site"));
                container.logger().info(clientRequest.uri().toString());
                container.logger().info(clientRequest.params().get("code"));
                // le paramètre reçu est-il un code?
                if(clientRequest.params().get("code")!=null){
                    eb.send("instagram.token", clientRequest.params().get("code"), new Handler<Message<String>>() {
                        @Override
                        public void handle(Message<String> event) {

                            //container.logger().info(event.body().toString());

                            // récupération du cookie utilisateur pour mettre le token au bon endroit en BDD
                            String cookie = clientRequest.headers().get("Cookie");

                            if(cookie!=null) {
                                JsonObject updateRequest = new JsonObject();
                                updateRequest.putString("action", "UPDATE");
                                updateRequest.putString("site", "insta_key");
                                updateRequest.putString("key", event.body().toString());
                                updateRequest.putString("token", cookie.substring(cookie.indexOf("=") + 1));
                                eb.send("DBHelper-auth", updateRequest, new Handler<Message<JsonObject>>() {
                                    @Override
                                    public void handle(Message<JsonObject> event) {
                                        //clientRequest.response().end(event.body().toString());
                                        JsonObject result = event.body();
                                        if(result.getInteger("number")==1){
                                            clientRequest.response().setStatusCode(301);
                                            clientRequest.response().putHeader("Location","http://localhost:8081/insta-test.html");
                                            clientRequest.response().end(event.body().encodePrettily());
                                        }else{
                                            clientRequest.response().end("error while saving datas");
                                        }
                                    }
                                });
                            }else{
                                clientRequest.response().end("pas de cookie");
                            }
                        }
                    });
                }
                //clientRequest.response().end("token bidon");
            }
        });




        routeMatcher.get("/twitter/:code", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest clientRequest) {
                String code = clientRequest.params().get("code");
                MultiMap params = clientRequest.params();
                container.logger().info(clientRequest.params().names().toString());
                if(code.equals("null"))code="";
                JsonObject req = new JsonObject();
                req.putString("code",code);
                if(code.equals("ok")){
                    req.putString("oauth_token", params.get("oauth_token"));
                    req.putString("oauth_verifier",params.get("oauth_verifier"));
                }
                eb.send("twitter.token", req, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> event) {
                        container.logger().info("request: "+event.body().toString());
                        if(event.body().getString("redirect")!=null) {
                            clientRequest.response().putHeader("Location", event.body().getString("redirect"));
                            clientRequest.response().setStatusCode(302);
                        }
                        clientRequest.response().end(event.body().toString());
                    }
                });
            }
        });

        /**
         * Gestion des actions en base de données
         */
        routeMatcher.post("/mongo/:action", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest clientRequest) {
                container.logger().info("in mongo action");
                final JsonObject usr = new JsonObject();
                //action demandée par l'API
                final String action = clientRequest.params().get("action");
                usr.putString("action", action);
                /**
                 * Handler qui récupére les paramètres post de la requête
                 */
                clientRequest.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        // mappage des clefs => valeurs de la requête
                        Map map = getQueryMap(buffer.toString());
                        // on récupére l'ensemble des éléments possible, même si il n'existent pas
                        if (action.equals("SIGN_UP") || action.equals("SIGN_IN")) {
                            usr.putString("password", map.get("password").toString());
                            usr.putString("email", map.get("email").toString());
                            if (action.equals("SIGN_UP")) {
                                usr.putString("userName", map.get("name").toString());

                            }
                        }
                        //usr.putString("token",map.get("token").toString());

                        /*
                        Traitement des infos en base de donnée
                         */
                        eb.send("DBHelper-auth", usr, new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> event) {
                                JsonObject obj = event.body();
                                container.logger().info("sign_up response: " + obj.toString());

                                /*
                                On assigne à l'utilisateur un cookie de session pour pouvoir utiliser ses token
                                Instagram ...
                                 */
                                if (obj.getString("cookie") != null) {
                                    container.logger().info("set cookie");
                                    //String cookie = req.headers().get("Cookie"); //La valeur contenue dans cookie
                                    clientRequest.response().putHeader("Set-Cookie", obj.getString("cookie"));
                                }

                                /*
                                On redirige l'utilisateur automatiquement, pour une meilleur navigation
                                 */
                                if (obj.getString("move") != null) {
                                    clientRequest.response().setStatusCode(301); // or SC_FOUND
                                    clientRequest.response().putHeader("Location", obj.getString("move"));

                                }

                                clientRequest.response().end(event.body().encodePrettily());
                            }
                        });

                    }
                });
            }
        });


        /**
         * Si aucune route ne correspond, on affiche le front-end
         */
        routeMatcher.noMatch(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {

                String file = "";
                if (req.path().equals("/")) {
                    file = "index.html";
                } else if (!req.path().contains("..")) {
                    file = req.path();
                }
                req.response().sendFile("webroot/" + file);
            }
        });

        vertx.createHttpServer().requestHandler(routeMatcher).listen(8081);
    }

    /**
     * Get user id session stored in cookie
     * @param event: user request
     * @return String: cookie if exist, null otherwise
     */
    private String getTokenCookie(HttpServerRequest event){
        String cookie = event.headers().get("Cookie"); //La valeur contenue dans cookie
        if(cookie!=null) {
            int substr = cookie.indexOf("=");
            if (substr != -1) {
                return cookie.substring(substr + 1);
            }
        }
        return null;
    }

    private void getUser(HttpServerRequest request){
        final String cookie = getTokenCookie(request);
        if(cookie!=null){

            vertx.eventBus().send("DBHelper-auth", new JsonObject("{\"action\":\"FIND\",\"token\":\"" + cookie + "\"}"), new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> authTest) {
                    if (authTest.body().getInteger("number") == 1) {
                        JsonObject obj = authTest.body().getArray("results").get(0);
                        User user = new User(obj);
                        container.logger().info(user.toJSON().toString());
                        ConcurrentMap<String, String> map = vertx.sharedData().getMap("storage.user");

                        map.put(cookie, user.toJSON().toString());
                    }

                }
            });
        }
    }

    public static Map<String, String> getQueryMap(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = "";

            try {
                value = URLDecoder.decode(param.split("=")[1], "UTF-8");

            } catch (Exception e) {
                System.out.println("wtf exception: " + e.getMessage());
            }


            map.put(name, value);
        }

        return map;
    }
}
