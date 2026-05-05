package com.ecommerce.stockapp.config;

public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static String url() {
        return value("DB_URL", "jdbc:mysql://localhost:3306/product_stock_management?useSSL=false&serverTimezone=UTC");
    }

    public static String user() {
        return value("DB_USER", "root");
    }

    public static String password() {
        return value("DB_PASSWORD", "");
    }

    private static String value(String key, String fallback) {
        String property = System.getProperty(key);
        if (property != null && !property.isBlank()) {
            return property;
        }
        String env = System.getenv(key);
        return env == null || env.isBlank() ? fallback : env;
    }
}
