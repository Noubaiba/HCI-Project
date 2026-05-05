package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.controller.AuthController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class ActivationLinkService {
    private final AuthController authController;
    private HttpServer server;

    public ActivationLinkService(AuthController authController) {
        this.authController = authController;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
            server.createContext("/activate", this::handleActivation);
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            System.err.println("Activation link server could not start on localhost:8080: " + e.getMessage());
        }
    }

    private void handleActivation(HttpExchange exchange) throws IOException {
        Optional<String> token = tokenFrom(exchange.getRequestURI().getRawQuery());
        String response;
        int status;
        if (token.isPresent()) {
            Platform.runLater(() -> authController.showActivationDialog(token.get()));
            status = 200;
            response = """
                    <html>
                    <body style="font-family:Segoe UI,Arial;padding:32px">
                    <h2>Activation ouverte</h2>
                    <p>Retournez dans l'application desktop pour definir votre mot de passe.</p>
                    </body>
                    </html>
                    """;
        } else {
            status = 400;
            response = "Token manquant.";
        }
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private Optional<String> tokenFrom(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(query.split("&"))
                .map(part -> part.split("=", 2))
                .filter(pair -> pair.length == 2 && "token".equals(pair[0]))
                .map(pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8))
                .findFirst();
    }
}
