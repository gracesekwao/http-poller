package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;
import java.time.Instant;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;


public class MainVerticle extends AbstractVerticle {

  private BackgroundPoller poller;
  private ServiceList serviceList;


  @Override
  public void start(Future<Void> startFuture) {
    poller = new BackgroundPoller(vertx);
    serviceList = new ServiceList(new DBConnector(vertx));

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

      try {
        JsonObject service = serviceObj(jsonBody.getString("url"), jsonBody.getString("name"));
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
      } catch (MalformedURLException e) {
        req.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid url: " + jsonBody.getString("url"));
      }
    });

    // delete services
    router.delete("/service/:service").handler(req -> {
      try {
        String service = URLDecoder.decode(req.pathParam("service"), "UTF-8");
        serviceList.remove(service).setHandler(asyncResult -> {
          if (asyncResult.succeeded()) {
            System.out.println("Service deleted successfully");
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("OK");
          } else {
            System.out.println("failed to delete service");
            req.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "text/plain")
                    .end("Internal error");
          }
        });
      } catch (UnsupportedEncodingException e) {
        req.response()
                .setStatusCode(400)
                .putHeader("content-type", "text/plain")
                .end("Invalid parameter: " + req.pathParam("service"));
      }
    });
  }

  private JsonObject serviceObj(String url, String name) throws MalformedURLException {
    return new JsonObject()
            .put("url", new URL(url).toString())
            .put("name", name != null ? name : url)
            .put("createdAt", Instant.now())
            .put("status", "UNKNOWN");
  }

}



