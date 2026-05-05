package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.model.CartItem;
import com.ecommerce.stockapp.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDao {
    private final Database database;

    public CartDao(Database database) {
        this.database = database;
    }

    public void addOrUpdate(int userId, int productId, int quantity) {
        String sql = """
                INSERT INTO cart(user_id, product_id, quantity) VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to add item to cart", e);
        }
    }

    public void updateQuantity(int cartId, int quantity) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE cart SET quantity = ? WHERE id = ?")) {
            statement.setInt(1, quantity);
            statement.setInt(2, cartId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to update cart", e);
        }
    }

    public void remove(int cartId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM cart WHERE id = ?")) {
            statement.setInt(1, cartId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to remove cart item", e);
        }
    }

    public void clear(int userId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM cart WHERE user_id = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    public List<CartItem> findByUser(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = """
                SELECT c.id AS cart_id, c.user_id, c.quantity AS cart_quantity,
                       p.*, cat.name AS category_name
                FROM cart c
                JOIN products p ON p.id = c.product_id
                LEFT JOIN categories cat ON cat.id = p.category_id
                WHERE c.user_id = ?
                ORDER BY p.name
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("image_url"),
                        rs.getBigDecimal("price"), rs.getInt("quantity"), rs.getInt("category_id"),
                        rs.getString("category_name")
                );
                items.add(new CartItem(rs.getInt("cart_id"), userId, product, rs.getInt("cart_quantity")));
            }
            return items;
        } catch (SQLException e) {
            throw new DaoException("Unable to list cart", e);
        }
    }
}