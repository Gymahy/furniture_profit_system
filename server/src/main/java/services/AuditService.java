package services;

import dao.AuditDAO;
import dao.impl.AuditDAOImpl;
import models.entities.AuditLog;

import java.util.List;

public class AuditService {
    private static final int DEFAULT_LIMIT = 300;

    private final AuditDAO auditDAO = new AuditDAOImpl();

    public List<AuditLog> findRecent() throws Exception {
        return auditDAO.findRecent(DEFAULT_LIMIT);
    }
}
