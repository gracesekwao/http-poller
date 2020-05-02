package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class BackgroundPoller {
  private final WebClient client;

  public BackgroundPoller(io.vertx.core.Vertx vertx) {
    client = WebClient.create(vertx);
  }
  public List<Future<JsonObject>> pollServices(List<JsonObject> services) {
    return services.parallelStream().map(this::poll).collect(Collectors.toList());
  }

  private Future<JsonObject> poll(JsonObject service) {
    String url = service.getString("url");
    Future<JsonObject> status = Future.future();
    try {
      client.getAbs(url)
              .send(response -> {
                if (response.succeeded()) {
                  status.complete(service.put("status", 200 == response.result().statusCode() ? "OK" : "FAIL"));
                } else {
                  status.complete(service.put("status", "FAIL"));
                }
              });
    } catch (Exception e) {
      status.complete(service.put("status", "FAIL"));
    }
    return status;
  }
}
