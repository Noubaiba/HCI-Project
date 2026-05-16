package com.ecommerce.stockapp.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;


public class ActivationLinkService {
    private final AuthService authService;
    private HttpServer server;
    private final Runnable afterActivation;

    	public ActivationLinkService(AuthService authService, Runnable afterActivation) {
        this.authService = authService;
        this.afterActivation = afterActivation;
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
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            showActivationForm(exchange);
            return;
        }
        if ("POST".equalsIgnoreCase(method)) {
            submitActivation(exchange);
            return;
        }
        send(exchange, 405, page("Method not allowed", "Use the activation form from your email.", null, false));
    }

    private void showActivationForm(HttpExchange exchange) throws IOException {
        Optional<String> token = tokenFrom(exchange.getRequestURI().getRawQuery());
        if (token.isEmpty()) {
            send(exchange, 400, page("Activation link invalid", "The activation token is missing.", null, true));
            return;
        }
        send(exchange, 200, form(token.get(), null));
    }

    private void submitActivation(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        String token = form.getOrDefault("token", "");
        String password = form.getOrDefault("password", "");
        String confirmPassword = form.getOrDefault("confirmPassword", "");

        if (!password.equals(confirmPassword)) {
            send(exchange, 400, form(token, "The two passwords do not match."));
            return;
        }

        try {
            authService.activateStockManager(token, password);
            Platform.runLater(afterActivation);
            send(exchange, 200, page(
                    "Account activated",
                    "Your password has been saved. Return to the desktop application and log in with your email and new password.",
                    "You can now close this browser tab.",
                    false
            ));
        } catch (RuntimeException e) {
            send(exchange, 400, form(token, e.getMessage()));
        }
    }

    private Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = new LinkedHashMap<>();
        if (body.isBlank()) {
            return values;
        }
        for (String part : body.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2) {
                values.put(decode(pair[0]), decode(pair[1]));
            }
        }
        return values;
    }

    private Optional<String> tokenFrom(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(query.split("&"))
                .map(part -> part.split("=", 2))
                .filter(pair -> pair.length == 2 && "token".equals(pair[0]))
                .map(pair -> decode(pair[1]))
                .findFirst();
    }

    private String form(String token, String error) {
        String errorBlock = error == null || error.isBlank()
                ? ""
                : "<div class=\"error\">" + escape(error) + "</div>";
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Activate stock manager account</title>
                  %s
                </head>
                <body>
                  <main class="card">
                    <div class="brand">Stockify</div>
                    <h1>Create your password</h1>
                    <p class="copy">Choose a password for your stock manager account. You will use it to log in to the desktop application.</p>
                    %s
                    <form method="post" action="/activate">
                      <input type="hidden" name="token" value="%s">
                      <label>
                        New password
                        <input type="password" name="password" minlength="8" required autofocus>
                      </label>
                      <label>
                        Confirm password
                        <input type="password" name="confirmPassword" minlength="8" required>
                      </label>
                      <button type="submit">Activate account</button>
                    </form>
                    <p class="hint">Password must contain at least 8 characters.</p>
                  </main>
                </body>
                </html>
                """.formatted(styles(), errorBlock, escape(token));
    }

    private String page(String title, String message, String hint, boolean danger) {
        String hintBlock = hint == null || hint.isBlank() ? "" : "<p class=\"hint\">" + escape(hint) + "</p>";
        String state = danger ? "danger" : "success";
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>%s</title>
                  %s
                </head>
                <body>
                  <main class="card %s">
                    <div class="brand">Stockify</div>
                    <h1>%s</h1>
                    <p class="copy">%s</p>
                    %s
                  </main>
                </body>
                </html>
                """.formatted(escape(title), styles(), state, escape(title), escape(message), hintBlock);
    }

    private String styles() {
        return """
                <style>
                  * { box-sizing: border-box; }
                  body {
                    margin: 0;
                    min-height: 100vh;
                    display: grid;
                    place-items: center;
                    background: #f4f7fb;
                    color: #172033;
                    font-family: "Segoe UI", Arial, sans-serif;
                  }
                  .card {
                    width: min(460px, calc(100vw - 32px));
                    background: white;
                    border: 1px solid #dfe7f1;
                    border-radius: 14px;
                    padding: 34px;
                    box-shadow: 0 18px 50px rgba(15, 23, 42, 0.10);
                  }
                  .brand {
                    color: #0f766e;
                    font-size: 14px;
                    font-weight: 800;
                    letter-spacing: 0.08em;
                    text-transform: uppercase;
                  }
                  h1 {
                    margin: 12px 0 10px;
                    font-size: 30px;
                    line-height: 1.15;
                  }
                  .copy, .hint {
                    color: #66758a;
                    font-size: 14px;
                    line-height: 1.55;
                  }
                  form {
                    display: grid;
                    gap: 16px;
                    margin-top: 22px;
                  }
                  label {
                    display: grid;
                    gap: 8px;
                    color: #34445c;
                    font-size: 13px;
                    font-weight: 800;
                  }
                  input {
                    width: 100%;
                    border: 1px solid #cfd8e5;
                    border-radius: 8px;
                    padding: 13px 14px;
                    font: inherit;
                  }
                  input:focus {
                    border-color: #0f766e;
                    outline: 3px solid rgba(15, 118, 110, 0.14);
                  }
                  button {
                    border: 0;
                    border-radius: 8px;
                    padding: 14px 18px;
                    background: #0f766e;
                    color: white;
                    font-size: 15px;
                    font-weight: 800;
                    cursor: pointer;
                  }
                  .error {
                    margin-top: 18px;
                    border-radius: 8px;
                    background: #fff1f1;
                    color: #b91c1c;
                    padding: 12px 14px;
                    font-size: 13px;
                    font-weight: 700;
                  }
                  .success { border-top: 5px solid #0f766e; }
                  .danger { border-top: 5px solid #dc3545; }
                </style>
                """;
    }

    private void send(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}