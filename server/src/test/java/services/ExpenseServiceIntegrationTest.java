package services;

import models.entities.Expense;
import models.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import utility.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class ExpenseServiceIntegrationTest {
    private final ExpenseService service = new ExpenseService();
    private final List<Integer> expenseIds = new ArrayList<>();
    private Integer categoryId;
    private Integer actorId;

    @AfterEach
    void cleanup() throws Exception {
        try (Connection connection = DBConnection.getConnection()) {
            for (Integer expenseId : expenseIds) {
                executeUpdate(connection, "DELETE FROM audit_log WHERE entity_type = 'expenses' AND entity_id = ?", expenseId);
                executeUpdate(connection, "DELETE FROM expenses WHERE id = ?", expenseId);
            }
            if (categoryId != null) {
                executeUpdate(connection, "DELETE FROM expense_categories WHERE id = ?", categoryId);
            }
            if (actorId != null) {
                executeUpdate(connection, "DELETE FROM audit_log WHERE user_id = ?", actorId);
                executeUpdate(connection, "DELETE FROM users WHERE id = ?", actorId);
            }
        }
    }

    @Test
    void savesExpenseInPeriod() throws Exception {
        setupFixture();
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);
        Expense expense = expense(LocalDate.of(2026, 5, 10), "Integration in range");

        Expense saved = service.save(expense, actor(), start, end);
        expenseIds.add(saved.getId());

        List<Expense> inRange = service.findByPeriod(start, end);
        List<Expense> outOfRange = service.findByPeriod(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertTrue(inRange.stream().anyMatch(row -> saved.getId().equals(row.getId())));
        assertFalse(outOfRange.stream().anyMatch(row -> saved.getId().equals(row.getId())));
    }

    @Test
    void rejectsExpenseOutsidePeriod() throws Exception {
        setupFixture();
        String description = "Integration outside range";
        Expense expense = expense(LocalDate.of(2026, 4, 30), description);
        int before = countExpensesByDescription(description);

        assertThrows(IllegalArgumentException.class,
                () -> service.save(expense, actor(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)));

        assertEquals(before, countExpensesByDescription(description));
    }

    private void setupFixture() throws Exception {
        try (Connection connection = DBConnection.getConnection()) {
            ensureRole(connection);
            actorId = insertUser(connection);
            categoryId = insertCategory(connection);
        }
    }

    private void ensureRole(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO roles(name)
                SELECT 'ACCOUNTANT'
                WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ACCOUNTANT')
                """)) {
            statement.executeUpdate();
        }
    }

    private int insertUser(Connection connection) throws Exception {
        int roleId;
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM roles WHERE name = 'ACCOUNTANT'");
             ResultSet resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next());
            roleId = resultSet.getInt("id");
        }

        String login = "Accountant_" + System.nanoTime();
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO users(login, password_hash, full_name, role_id, is_active)
                VALUES (?, ?, ?, ?, TRUE)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, login);
            statement.setString(2, "test-hash");
            statement.setString(3, "Accountant");
            statement.setInt(4, roleId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                return keys.getInt(1);
            }
        }
    }

    private Integer insertCategory(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO expense_categories(name, description)
                VALUES (?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, "Expense category " + System.nanoTime());
            statement.setString(2, "Temporary category for integration tests");
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                return keys.getInt(1);
            }
        }
    }

    private Expense expense(LocalDate date, String description) {
        Expense expense = new Expense();
        expense.setExpenseDate(date);
        expense.setAmount(new BigDecimal("123.65"));
        expense.setCategoryId(categoryId);
        expense.setDescription(description);
        return expense;
    }

    private User actor() {
        User user = new User();
        user.setId(actorId);
        return user;
    }

    private int countExpensesByDescription(String description) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT COUNT(*) FROM expenses
                     WHERE description = ? AND category_id = ?
                     """)) {
            statement.setString(1, description);
            statement.setInt(2, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getInt(1);
            }
        }
    }

    private void executeUpdate(Connection connection, String sql, Integer id) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
}
