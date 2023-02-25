package org.zalando.logbook.openfeign;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public abstract class FeignHttpServerRunner {
    protected static HttpServer server;

    @BeforeAll
    static void setupServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/get/string", exchange -> {
                    String response = "response";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
        );
        server.createContext("/post/bad-request", exchange -> {
                    String response = "response";
                    exchange.sendResponseHeaders(400, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
        );
        server.createContext("/get/void", exchange -> {
            exchange.sendResponseHeaders(401, -1);
        });
        server.setExecutor(null);
        server.start();
    }

    @AfterAll
    static void destroyServer() {
        server.stop(1);
    }
}
