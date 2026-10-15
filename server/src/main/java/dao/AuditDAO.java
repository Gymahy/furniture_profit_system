package dao;

import models.entities.AuditLog;

import java.util.List;

public interface AuditDAO {
    void log(Integer userId, String action, String entityType, Integer entityId, String details);

    List<AuditLog> findRecent(int limit) throws Exception;
}
