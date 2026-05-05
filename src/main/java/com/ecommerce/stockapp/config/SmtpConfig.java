package com.ecommerce.stockapp.config;

public final class SmtpConfig {
    private SmtpConfig() {}

    public static String host() { return value("SMTP_HOST", "smtp.gmail.com"); }
    public static int port() { return Integer.parseInt(value("SMTP_PORT", "587")); }
    public static String username() { return value("SMTP_USER", "noubaiba.lahyaouine@uit.ac.ma"); }
    public static String password() { return value("SMTP_PASSWORD", ""); }
    public static String from() { return value("SMTP_FROM", username()); }
    public static String activationBaseUrl() { return value("APP_ACTIVATION_BASE_URL", "http://localhost:8080/activate"); }

    public static boolean configured() {
        return !username().isBlank() && !password().isBlank() && !from().isBlank();
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
