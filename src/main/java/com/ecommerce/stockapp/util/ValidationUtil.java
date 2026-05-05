package com.ecommerce.stockapp.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private ValidationUtil() {}

    public static void requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
    }

    public static void requireEmail(String email) {
        requireText(email, "Email");
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
    }

    public static void requirePassword(String password) {
        requireText(password, "Password");
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
    }

    public static BigDecimal money(String value) {
        try {
            BigDecimal amount = new BigDecimal(value);
            if (amount.signum() < 0) {
                throw new IllegalArgumentException("Price cannot be negative.");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid price.");
        }
    }

    public static int positiveInt(String value, String label) {
        try {
            int number = Integer.parseInt(value);
            if (number < 0) {
                throw new IllegalArgumentException(label + " cannot be negative.");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
    }
}
