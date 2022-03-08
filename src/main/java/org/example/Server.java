package org.example;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class Server extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Server server = new Server();
        vertx.deployVerticle(server);
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        String host = "0.0.0.0";
        int port = 8080;
        HttpServerOptions options = new HttpServerOptions();
        options.setLogActivity(true);
        HttpServer hs = vertx.createHttpServer(options).requestHandler(router);
        hs.websocketHandler(this::wsHandler);
        hs.listen(port, host, (done) -> {
            if (done.failed()) {
            }
            LOGGER.info("Started API server 3.6.3");
        });
    }
    private void wsHandler(ServerWebSocket webSocket) {
        MultiMap headers = webSocket.headers();
        String cookies = headers.get("Cookie");
        HttpResponseStatus status = HttpResponseStatus.ACCEPTED;
         if(cookies == null || cookies == "") {
            status = HttpResponseStatus.UNAUTHORIZED;
        }
        if(!cookies.startsWith("GoodActor")) {
            status = HttpResponseStatus.FORBIDDEN;
        }
        if (!status.equals(HttpResponseStatus.ACCEPTED)) {
            webSocket.reject(status.code());
            LOGGER.error("client failed: " + headers);
            LOGGER.error("========================");
            return;
        }
        LOGGER.info("client connected");
        webSocket.accept();
        webSocket.writeTextMessage("Welcome to simple websocket.");

        webSocket.frameHandler(frame -> {
            LOGGER.info("Got frame: " + frame.textData());
        });
        webSocket.closeHandler(message -> {
            LOGGER.info("Client disconnected " + webSocket.textHandlerID());
        });
    }
}
