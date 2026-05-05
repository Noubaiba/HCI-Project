package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.model.Role;
import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.model.UserStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public class UserDao {
    private final Database database;

    public UserDao(Database database) {
        this.database = database;
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Unable to find user by email", e);
        }
    }

    public Optional<User> findByActivationToken(String token) {
        String sql = "SELECT * FROM users WHERE activation_token = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            ResultSet rs = statement.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Unable to find activation token", e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, name";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DaoException("Unable to list users", e);
        }
    }

    public int create(User user) {
        // Ajout de created_at dans l'insertion initiale
        String sql = "INSERT INTO users(name, email, password, role, status, activation_token, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole().name());
            statement.setString(5, user.getStatus().name());
            statement.setString(6, user.getActivationToken());
            statement.setTimestamp(7, Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));

            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Unable to create user", e);
        }
    }

    public void updateStatus(int id, UserStatus status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to update user status", e);
        }
    }

    public void updateRole(int id, Role role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to update user role", e);
        }
    }

    public void activate(String token, String passwordHash) {
        String sql = "UPDATE users SET password = ?, status = 'ACTIVE', activation_token = NULL WHERE activation_token = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setString(2, token);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to activate account", e);
        }
    }

    /**
     * Met à jour les informations de profil (Nom, Tel, Adresse, Image)
     */
    public void updateProfile(User user) {
        String sql = "UPDATE users SET name = ?, phone = ?, delivery_address = ?, profile_picture = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getPhone());
            statement.setString(3, user.getDeliveryAddress());
            statement.setString(4, user.getProfilePicture());
            statement.setInt(5, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la mise à jour du profil", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime ldt = (ts != null) ? ts.toLocalDateTime() : null;

        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role")),
                UserStatus.valueOf(rs.getString("status")),
                rs.getString("activation_token"),
                ldt,
                rs.getString("phone"),
                rs.getString("delivery_address"),
                rs.getString("profile_picture")
        );
    }
}