package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.model.StockMovement;
import com.ecommerce.stockapp.model.StockMovementType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockMovementDao {
    private final Database database;

    public StockMovementDao(Database database) {
        this.database = database;
    }

    public void create(int productId, StockMovementType type, int quantity) {
        String sql = "INSERT INTO stock_movements(product_id, type, quantity, date) VALUES (?, ?, ?, NOW())";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setString(2, type.name());
            statement.setInt(3, quantity);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to create stock movement", e);
        }
    }

    public void create(Connection connection, int productId, StockMovementType type, int quantity) throws SQLException {
        String sql = "INSERT INTO stock_movements(product_id, type, quantity, date) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setString(2, type.name());
            statement.setInt(3, quantity);
            statement.executeUpdate();
        }
    }

    public List<StockMovement> findRecent() {
        List<StockMovement> movements = new ArrayList<>();
        String sql = """
                SELECT sm.id, p.name AS product_name, sm.type, sm.quantity, sm.date
                FROM stock_movements sm JOIN products p ON p.id = sm.product_id
                ORDER BY sm.date DESC LIMIT 200
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                movements.add(new StockMovement(
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        StockMovementType.valueOf(rs.getString("type")),
                        rs.getInt("quantity"),
                        rs.getTimestamp("date").toLocalDateTime()
                ));
            }
            return movements;
        } catch (SQLException e) {
            throw new DaoException("Unable to list stock history", e);
        }
    }
}
