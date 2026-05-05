package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.url(), DatabaseConfig.user(), DatabaseConfig.password());
    }
}
