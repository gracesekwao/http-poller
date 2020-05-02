package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

import io.vertx.ext.sql.ResultSet;
import se.kry.codetest.DBConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;


public class ServiceList {
    private final DBConnector connector;

    private final HashMap<String, JsonObject> services = new HashMap<>();

    public ServiceList(DBConnector connector) {
        this.connector = connector;
    }

    public Future<Boolean> init() {
        Future<Boolean> statusFuture = Future.future();
        connector.query("SELECT * FROM services;").setHandler(res -> {
            if (res.succeeded()) {
                res.result().getRows().forEach(row -> services.put(row.getString("url"), row.put("status", "UNKNOWN")));
                statusFuture.complete(true);
            } else {
                System.out.println("DB connection issue" + res.cause());
                statusFuture.fail(res.cause());
            }
        });
        return statusFuture;
    }

    public List<JsonObject> getServices() {
        return new ArrayList<>(services.values());
    }

    public Future<ResultSet> insert(JsonObject service) {
        services.put(service.getString("url"), service);
        return connector.query("INSERT OR REPLACE INTO service (url, name, createdAt)" +
                        " values (?," +
                        " ?," +
                        " COALESCE((SELECT createdAt FROM service WHERE url = ?), ?)" +
                        ")",
                new JsonArray()
                        .add(service.getString("url"))
                        .add(service.getString("name"))
                        .add(service.getString("createdAt"))
        );
    }

    public Future<ResultSet> remove(String service) {
        services.remove(service);
        return connector.query("DELETE FROM service WHERE url=?", new JsonArray().add(service));
    }

}
