package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.ServiceList;

import java.util.HashMap;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

public class MainVerticle extends AbstractVerticle {

  private BackgroundPoller poller;
  private ServiceList serviceList;

  private HashMap<String, String> services = new HashMap<>();
  private DBConnector connector;

  @Override
  public void start(Future<Void> startFuture) {
    poller = new BackgroundPoller(vertx);
    serviceList = new ServiceList(new DBConnector(vertx));

    connector = new DBConnector(vertx);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    serviceList.init().setHandler(result -> {
      if (result.succeeded()) {
        vertx.setPeriodic(1000 * 6, timerId -> poller.pollServices(serviceList.getServices()));
        setRoutes(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, res -> {
                  if (res.succeeded()) {
                    System.out.println("KRY code test service started");
                    startFuture.complete();
                  } else {
                    startFuture.fail(res.cause());
                  }
                });
      } else {
        startFuture.fail(result.cause());
      }
    });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());

    // get services
    router.get("/service").handler(req -> {
      List<JsonObject> jsonServices = serviceList.getServices();
      req.response()
              .putHeader("content-type", "application/json")
              .end(new JsonArray(jsonServices).encode());
    });

    // post services
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      JsonObject service;
      try {
        service =serviceObj(jsonBody.getString("url"), jsonBody.getString("name"));
      } catch (MalformedURLException e) {
        req.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid url: " + jsonBody.getString("url"));
        return;
      }
      serviceList.insert(service).setHandler(asyncResult -> {
        if (asyncResult.succeeded()) {
          System.out.println("URL Post successful");
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("OK");
        } else {
          System.out.println("URL Post failed" + asyncResult.cause());
          req.response()
                  .setStatusCode(500)
                  .putHeader("content-type", "text/plain")
                  .end("Internal error");
        }
      });
    });

    // delete services

  }

  private JsonObject serviceObj(String url, String name) throws MalformedURLException {
    return new JsonObject()
            .put("url", new URL(url).toString())
            .put("name", name != null ? name : url)
            .put("createdAt", Instant.now())
            .put("status", "UNKNOWN");
  }

}



