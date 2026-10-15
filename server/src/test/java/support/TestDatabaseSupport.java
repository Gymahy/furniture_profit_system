package support;

import enums.RoleType;
import utility.DBConnection;
import utility.PasswordHasher;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestDatabaseSupport {
    private TestDatabaseSupport() {
    }

    public static Connection connection() throws Exception {
        return DBConnection.getConnection();
    }

    public static int ensureRole(Connection connection, RoleType roleType) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO roles(name)
                SELECT ?
                WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = ?)
                """)) {
            statement.setString(1, roleType.name());
            statement.setString(2, roleType.name());
            statement.executeUpdate();
        }

        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM roles WHERE name = ?")) {
            statement.setString(1, roleType.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getInt("id");
            }
        }
    }

    public static int insertUser(Connection connection, RoleType roleType, String loginPrefix) throws Exception {
        return insertUser(connection, roleType, loginPrefix, PasswordHasher.hash("test12345"));
    }

    public static int insertUser(Connection connection, RoleType roleType, String loginPrefix, String passwordHash) throws Exception {
        int roleId = ensureRole(connection, roleType);
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO users(login, password_hash, full_name, role_id, is_active)
                VALUES (?, ?, ?, ?, TRUE)
                """, Statement.RETURN_GENERATED_KEYS)) {
            String unique = unique(loginPrefix);
            statement.setString(1, unique);
            statement.setString(2, passwordHash);
            statement.setString(3, "JUnit " + roleType.name());
            statement.setInt(4, roleId);
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    public static int insertCategory(Connection connection, String namePrefix) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO categories(name, description)
                VALUES (?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, unique(namePrefix));
            statement.setString(2, "Temporary category for JUnit tests");
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    public static int insertProduct(Connection connection, int categoryId, int stockQuantity, String namePrefix) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO products(name, article, stock_quantity, category_id, purchase_price, retail_price)
                VALUES (?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            String unique = unique(namePrefix);
            statement.setString(1, unique);
            statement.setString(2, unique("ART"));
            statement.setInt(3, stockQuantity);
            statement.setInt(4, categoryId);
            statement.setBigDecimal(5, new BigDecimal("100.00"));
            statement.setBigDecimal(6, new BigDecimal("150.00"));
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    public static int insertCustomer(Connection connection, String namePrefix) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO customers(full_name, phone, customer_type, registration_date)
                VALUES (?, ?, 'individual', ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, unique(namePrefix));
            statement.setString(2, "+375" + Math.abs(System.nanoTime()));
            statement.setDate(3, Date.valueOf(LocalDate.now()));
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    public static int productStock(Connection connection, int productId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT stock_quantity FROM products WHERE id = ?")) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getInt("stock_quantity");
            }
        }
    }

    public static void executeUpdate(Connection connection, String sql, int id) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public static void deleteSaleGraph(Connection connection, int saleId) throws Exception {
        executeUpdate(connection, "DELETE FROM audit_log WHERE entity_type = 'sales' AND entity_id = ?", saleId);
        executeUpdate(connection, "DELETE FROM commissions WHERE sale_id = ?", saleId);
        executeUpdate(connection, "DELETE FROM sale_items WHERE sale_id = ?", saleId);
        executeUpdate(connection, "DELETE FROM sales WHERE id = ?", saleId);
    }

    public static void deleteProduct(Connection connection, int productId) throws Exception {
        executeUpdate(connection, "DELETE FROM audit_log WHERE entity_type = 'products' AND entity_id = ?", productId);
        executeUpdate(connection, "DELETE FROM products WHERE id = ?", productId);
    }

    public static void deleteCategory(Connection connection, int categoryId) throws Exception {
        executeUpdate(connection, "DELETE FROM audit_log WHERE entity_type = 'categories' AND entity_id = ?", categoryId);
        executeUpdate(connection, "DELETE FROM categories WHERE id = ?", categoryId);
    }

    public static void deleteCustomer(Connection connection, int customerId) throws Exception {
        executeUpdate(connection, "DELETE FROM audit_log WHERE entity_type = 'customers' AND entity_id = ?", customerId);
        executeUpdate(connection, "DELETE FROM customers WHERE id = ?", customerId);
    }

    public static void deleteUser(Connection connection, int userId) throws Exception {
        executeUpdate(connection, "DELETE FROM audit_log WHERE user_id = ?", userId);
        executeUpdate(connection, "DELETE FROM users WHERE id = ?", userId);
    }

    public static String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }

    private static int generatedId(PreparedStatement statement) throws Exception {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            assertTrue(keys.next());
            return keys.getInt(1);
        }
    }
}
