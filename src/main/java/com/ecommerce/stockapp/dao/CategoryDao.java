package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private final Database database;

    public CategoryDao(Database database) {
        this.database = database;
    }

    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM categories ORDER BY name");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
            return categories;
        } catch (SQLException e) {
            throw new DaoException("Unable to list categories", e);
        }
    }

    public void create(String name) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO categories(name) VALUES (?)")) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to create category", e);
        }
    }

    public void delete(int id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM categories WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to delete category", e);
        }
    }
}
