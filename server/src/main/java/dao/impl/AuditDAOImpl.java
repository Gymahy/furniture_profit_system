package dao.impl;

import dao.AuditDAO;
import models.entities.AuditLog;
import utility.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuditDAOImpl implements AuditDAO {
    @Override
    public void log(Integer userId, String action, String entityType, Integer entityId, String details) {
        String sql = "INSERT INTO audit_log(user_id, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, userId);
            }
            statement.setString(2, action);
            statement.setString(3, entityType);
            if (entityId == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, entityId);
            }
            statement.setString(5, details);
            statement.executeUpdate();
        } catch (Exception e) {
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }

    @Override
    public List<AuditLog> findRecent(int limit) throws Exception {
        List<AuditLog> logs = new ArrayList<>();
        String sql = """
                SELECT a.*, COALESCE(NULLIF(u.full_name, ''), u.login, 'Система') user_name
                FROM audit_log a
                LEFT JOIN users u ON u.id = a.user_id
                ORDER BY a.created_at DESC, a.id DESC
                LIMIT ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(1, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(resultSet.getInt("id"));
                    int userId = resultSet.getInt("user_id");
                    log.setUserId(resultSet.wasNull() ? null : userId);
                    log.setUserName(resultSet.getString("user_name"));
                    log.setAction(resultSet.getString("action"));
                    log.setEntityType(resultSet.getString("entity_type"));
                    int entityId = resultSet.getInt("entity_id");
                    log.setEntityId(resultSet.wasNull() ? null : entityId);
                    log.setDetails(resultSet.getString("details"));
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    log.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
                    logs.add(log);
                }
            }
        }
        return logs;
    }
}
