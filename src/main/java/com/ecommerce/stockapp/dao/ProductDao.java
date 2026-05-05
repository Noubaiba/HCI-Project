package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {
    private final Database database;

    public ProductDao(Database database) {
        this.database = database;
    }

    public List<Product> findAll(String search) {
        List<Product> products = new ArrayList<>();
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON c.id = p.category_id
                WHERE ? = '' OR p.name LIKE ? OR p.description LIKE ? OR c.name LIKE ?
                ORDER BY p.name
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String normalized = search == null ? "" : search.trim();
            String pattern = "%" + normalized + "%";
            statement.setString(1, normalized);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                products.add(map(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new DaoException("Unable to list products", e);
        }
    }

    public Optional<Product> findById(int id) {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p LEFT JOIN categories c ON c.id = p.category_id
                WHERE p.id = ?
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Unable to find product", e);
        }
    }

    public void save(Product product) {
        if (product.getId() == 0) {
            create(product);
        } else {
            update(product);
        }
    }

    public void create(Product product) {
        String sql = "INSERT INTO products(name, description, image_url, price, quantity, category_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fill(statement, product);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to create product", e);
        }
    }

    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, image_url = ?, price = ?, quantity = ?, category_id = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fill(statement, product);
            statement.setInt(7, product.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to update product", e);
        }
    }

    public void updateQuantity(int productId, int quantity) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE products SET quantity = ? WHERE id = ?")) {
            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to update product quantity", e);
        }
    }

    public void updateQuantity(Connection connection, int productId, int quantity) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE products SET quantity = ? WHERE id = ?")) {
            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
    }

    public void delete(int id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to delete product", e);
        }
    }

    public List<Product> lowStock(int threshold) {
        List<Product> products = new ArrayList<>();
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p LEFT JOIN categories c ON c.id = p.category_id
                WHERE p.quantity <= ?
                ORDER BY p.quantity ASC
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, threshold);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                products.add(map(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new DaoException("Unable to list low-stock products", e);
        }
    }

    private void fill(PreparedStatement statement, Product product) throws SQLException {
        statement.setString(1, product.getName());
        statement.setString(2, product.getDescription());
        statement.setString(3, product.getImageUrl());
        statement.setBigDecimal(4, product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
        statement.setInt(5, product.getQuantity());
        statement.setInt(6, product.getCategoryId());
    }

    private Product map(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("image_url"),
                rs.getBigDecimal("price"),
                rs.getInt("quantity"),
                rs.getInt("category_id"),
                rs.getString("category_name")
        );
    }
}