package dao.impl;

import dao.UserDAO;
import enums.RoleType;
import models.entities.Role;
import models.entities.User;
import utility.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    @Override
    public Optional<User> findByLogin(String login) throws Exception {
        String sql = "SELECT u.*, r.name role_name FROM users u LEFT JOIN roles r ON r.id = u.role_id WHERE u.login = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Integer id) throws Exception {
        String sql = "SELECT u.*, r.name role_name FROM users u LEFT JOIN roles r ON r.id = u.role_id WHERE u.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws Exception {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.name role_name FROM users u LEFT JOIN roles r ON r.id = u.role_id ORDER BY u.full_name, u.login";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }
        return users;
    }

    @Override
    public User save(User entity) throws Exception {
        if (entity.isNew()) {
            return insert(entity);
        }
        update(entity);
        return entity;
    }

    @Override
    public void delete(Integer id) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Нельзя удалить пользователя: он связан с продажами, комиссиями или другими документами. Заблокируйте учетную запись.");
        }
    }

    @Override
    public List<Role> findRoles() throws Exception {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT id, name FROM roles ORDER BY id";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                roles.add(new Role(resultSet.getInt("id"), RoleType.fromDbName(resultSet.getString("name"))));
            }
        }
        return roles;
    }

    @Override
    public Optional<Role> findRole(RoleType roleType) throws Exception {
        String sql = "SELECT id, name FROM roles WHERE name = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roleType.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Role(resultSet.getInt("id"), RoleType.fromDbName(resultSet.getString("name"))));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void setActive(Integer userId, boolean active) throws Exception {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    @Override
    public void updatePassword(Integer userId, String passwordHash) throws Exception {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    @Override
    public void ensureDefaultRoles() throws Exception {
        String sql = "INSERT IGNORE INTO roles(name) VALUES (?), (?), (?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, RoleType.ADMIN.name());
            statement.setString(2, RoleType.SALES_MANAGER.name());
            statement.setString(3, RoleType.ACCOUNTANT.name());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean hasUsers() throws Exception {
        String sql = "SELECT COUNT(*) total FROM users";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt("total") > 0;
        }
    }

    private User insert(User user) throws Exception {
        String sql = "INSERT INTO users(login, password_hash, full_name, role_id, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            if (user.getRoleId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, user.getRoleId());
            }
            statement.setBoolean(5, user.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        }
        return user;
    }

    private void update(User user) throws Exception {
        String sql = "UPDATE users SET login = ?, full_name = ?, role_id = ?, is_active = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getFullName());
            if (user.getRoleId() == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, user.getRoleId());
            }
            statement.setBoolean(4, user.isActive());
            statement.setInt(5, user.getId());
            statement.executeUpdate();
        }
    }

    private User mapUser(ResultSet resultSet) throws Exception {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setLogin(resultSet.getString("login"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setFullName(resultSet.getString("full_name"));
        int roleId = resultSet.getInt("role_id");
        user.setRoleId(resultSet.wasNull() ? null : roleId);
        String roleName = resultSet.getString("role_name");
        user.setRole(roleName == null ? null : RoleType.fromDbName(roleName));
        user.setActive(resultSet.getBoolean("is_active"));
        return user;
    }
}
