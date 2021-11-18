package org.vertx.test.infinispan;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.Date;

public class ServerVerticle extends AbstractVerticle {

  public static final String TEMPLATE = ""
    + "Session [%s] created on %s%n"
    + "%n"
    + "Page generated on %s%n";

  public static void main(String[] args) {
    ClusterManager mgr = new InfinispanClusterManager();

    VertxOptions options = new VertxOptions().setClusterManager(mgr);

    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        vertx.deployVerticle(new ServerVerticle());
      } else {
        // failed!
      }
    });


  }

  @Override
  public void start() {
    Router router = Router.router(vertx);

    SessionStore store = ClusteredSessionStore.create(vertx);

    router.route().handler(SessionHandler.create(store));

    router.get("/").handler(ctx -> {
      Session session = ctx.session();
      session.computeIfAbsent("createdOn", s -> System.currentTimeMillis());

      String sessionId = session.id();
      Date createdOn = new Date(session.<Long>get("createdOn"));
      Date now = new Date();

      ctx.end(String.format(TEMPLATE, sessionId, createdOn, now));
    });

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080);
  }
}
