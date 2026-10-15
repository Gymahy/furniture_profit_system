package dao.impl;

import dao.ExpenseDAO;
import models.entities.Expense;
import models.entities.ExpenseCategory;
import utility.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAOImpl implements ExpenseDAO {
    @Override
    public List<ExpenseCategory> findCategories() throws Exception {
        List<ExpenseCategory> categories = new ArrayList<>();
        String sql = "SELECT id, name, description FROM expense_categories ORDER BY name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(new ExpenseCategory(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("description")));
            }
        }
        return categories;
    }

    @Override
    public ExpenseCategory saveCategory(ExpenseCategory category) throws Exception {
        if (category.isNew()) {
            String sql = "INSERT INTO expense_categories(name, description) VALUES (?, ?)";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, category.getName());
                statement.setString(2, category.getDescription());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        category.setId(keys.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE expense_categories SET name = ?, description = ? WHERE id = ?";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, category.getName());
                statement.setString(2, category.getDescription());
                statement.setInt(3, category.getId());
                statement.executeUpdate();
            }
        }
        return category;
    }

    @Override
    public void deleteCategory(Integer id) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM expense_categories WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Нельзя удалить категорию расходов: она используется в расходах");
        }
    }

    @Override
    public boolean isCategoryUsed(Integer id) throws Exception {
        if (id == null) {
            return false;
        }
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM expenses WHERE category_id = ? LIMIT 1")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public List<Expense> findByPeriod(LocalDate startDate, LocalDate endDate) throws Exception {
        List<Expense> expenses = new ArrayList<>();
        String sql = """
                SELECT e.*, ec.name category_name, u.full_name user_name
                FROM expenses e
                LEFT JOIN expense_categories ec ON ec.id = e.category_id
                LEFT JOIN users u ON u.id = e.user_id
                WHERE e.expense_date BETWEEN ? AND ?
                ORDER BY e.expense_date DESC, e.id DESC
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    expenses.add(mapExpense(resultSet));
                }
            }
        }
        return expenses;
    }

    @Override
    public List<Expense> findAll() throws Exception {
        return findByPeriod(LocalDate.of(1900, 1, 1), LocalDate.of(2999, 12, 31));
    }

    @Override
    public Expense save(Expense entity) throws Exception {
        if (entity.isNew()) {
            String sql = "INSERT INTO expenses(expense_date, amount, category_id, description, user_id) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillExpenseStatement(statement, entity);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        entity.setId(keys.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE expenses SET expense_date = ?, amount = ?, category_id = ?, description = ?, user_id = ? WHERE id = ?";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                fillExpenseStatement(statement, entity);
                statement.setInt(6, entity.getId());
                statement.executeUpdate();
            }
        }
        return entity;
    }

    @Override
    public void delete(Integer id) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM expenses WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void fillExpenseStatement(PreparedStatement statement, Expense expense) throws Exception {
        statement.setDate(1, Date.valueOf(expense.getExpenseDate()));
        statement.setBigDecimal(2, expense.getAmount());
        if (expense.getCategoryId() == null) {
            statement.setNull(3, java.sql.Types.INTEGER);
        } else {
            statement.setInt(3, expense.getCategoryId());
        }
        statement.setString(4, expense.getDescription());
        if (expense.getUserId() == null) {
            statement.setNull(5, java.sql.Types.INTEGER);
        } else {
            statement.setInt(5, expense.getUserId());
        }
    }

    private Expense mapExpense(ResultSet resultSet) throws Exception {
        Expense expense = new Expense();
        expense.setId(resultSet.getInt("id"));
        Date expenseDate = resultSet.getDate("expense_date");
        expense.setExpenseDate(expenseDate == null ? null : expenseDate.toLocalDate());
        expense.setAmount(resultSet.getBigDecimal("amount"));
        int categoryId = resultSet.getInt("category_id");
        expense.setCategoryId(resultSet.wasNull() ? null : categoryId);
        expense.setCategoryName(resultSet.getString("category_name"));
        expense.setDescription(resultSet.getString("description"));
        int userId = resultSet.getInt("user_id");
        expense.setUserId(resultSet.wasNull() ? null : userId);
        expense.setUserName(resultSet.getString("user_name"));
        return expense;
    }
}
