package org.vertx.test.infinispan;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import java.util.Date;

public class ServerVerticle extends AbstractVerticle implements Handler<Message<String>> {

  public static void main(String[] args) {
    Vertx.clusteredVertx(new VertxOptions()).compose(vertx -> vertx.deployVerticle(new ServerVerticle())).onComplete(ar -> {
      if (ar.succeeded()) {
        System.out.println(">>> Verticle deployed");
      } else {
        ar.cause().printStackTrace();
      }
    });
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.eventBus().consumer("foo", this).completionHandler(ar -> {
      startPromise.handle(ar);
      vertx.eventBus().publish("foo", "Server started " + new Date());
    });
  }

  @Override
  public void handle(Message<String> msg) {
    System.out.println("msg = " + msg.body());
  }
}
