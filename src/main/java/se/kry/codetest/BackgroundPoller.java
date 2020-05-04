package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

 enum Response{

  OK {
    @Override
    public String toString() {
      return "OK";
    }
  },
  FAIL {
    @Override
    public String toString() {
      return "FAIL";
    }
  },

}
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
                  status.complete(service.put("status", 200 == response.result().statusCode() ? Response.OK : Response.FAIL));
                } else {
                  status.complete(service.put("status", Response.FAIL));
                }
              });
    } catch (Exception e) {
      status.complete(service.put("status", Response.FAIL));
    }
    return status;
  }
}
