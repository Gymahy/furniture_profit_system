package models.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private Integer userId;
    private String userName;
    private String action;
    private String entityType;
    private Integer entityId;
    private String details;
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionTitle() {
        if (action == null) {
            return "";
        }
        return switch (action) {
            case "LOGIN" -> "Вход";
            case "LOGIN_FAILED" -> "Ошибка входа";
            case "BOOTSTRAP_ADMIN" -> "Создан администратор";
            case "CREATE_USER" -> "Создан пользователь";
            case "UPDATE_USER" -> "Изменен пользователь";
            case "BLOCK_USER" -> "Пользователь заблокирован";
            case "UNBLOCK_USER" -> "Пользователь разблокирован";
            case "RESET_PASSWORD" -> "Смена пароля";
            case "DELETE_USER" -> "Удален пользователь";
            case "SAVE_CATEGORY" -> "Сохранена категория";
            case "DELETE_CATEGORY" -> "Удалена категория";
            case "SAVE_PRODUCT" -> "Сохранен товар";
            case "DELETE_PRODUCT" -> "Удален товар";
            case "SAVE_CUSTOMER" -> "Сохранен клиент";
            case "DELETE_CUSTOMER" -> "Удален клиент";
            case "CREATE_SALE" -> "Создана продажа";
            case "CHANGE_SALE_STATUS" -> "Изменен статус продажи";
            case "DELETE_SALE" -> "Удалена продажа";
            case "SAVE_EXPENSE" -> "Сохранен расход";
            case "DELETE_EXPENSE" -> "Удален расход";
            case "SAVE_EXPENSE_CATEGORY" -> "Сохранена статья расходов";
            case "DELETE_EXPENSE_CATEGORY" -> "Удалена статья расходов";
            case "SAVE_DISCOUNT" -> "Сохранена скидка";
            case "DELETE_DISCOUNT" -> "Удалена скидка";
            case "EXPORT_REPORT" -> "Экспорт отчета";
            default -> action;
        };
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAtFormatted() {
        return createdAt == null ? "" : createdAt.format(DATE_TIME_FORMATTER);
    }
}
