package dao.impl;

import dao.DiscountDAO;
import enums.DiscountType;
import models.entities.Discount;
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
import java.util.Optional;

public class DiscountDAOImpl implements DiscountDAO {
    @Override
    public Optional<Discount> findById(Integer id) throws Exception {
        String sql = "SELECT * FROM discounts WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapDiscount(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Discount> findActive(LocalDate date) throws Exception {
        List<Discount> discounts = new ArrayList<>();
        String sql = """
                SELECT * FROM discounts
                WHERE is_active = TRUE
                  AND (start_date IS NULL OR start_date <= ?)
                  AND (end_date IS NULL OR end_date >= ?)
                ORDER BY name
                """;
        LocalDate effectiveDate = date == null ? LocalDate.now() : date;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(effectiveDate));
            statement.setDate(2, Date.valueOf(effectiveDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    discounts.add(mapDiscount(resultSet));
                }
            }
        }
        return discounts;
    }

    @Override
    public List<Discount> findAll() throws Exception {
        List<Discount> discounts = new ArrayList<>();
        String sql = "SELECT * FROM discounts ORDER BY is_active DESC, name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                discounts.add(mapDiscount(resultSet));
            }
        }
        return discounts;
    }

    @Override
    public Discount save(Discount entity) throws Exception {
        if (entity.isNew()) {
            String sql = "INSERT INTO discounts(name, type, value, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillDiscountStatement(statement, entity);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        entity.setId(keys.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE discounts SET name = ?, type = ?, value = ?, start_date = ?, end_date = ?, is_active = ? WHERE id = ?";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                fillDiscountStatement(statement, entity);
                statement.setInt(7, entity.getId());
                statement.executeUpdate();
            }
        }
        return entity;
    }

    @Override
    public void delete(Integer id) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM discounts WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Нельзя удалить скидку: она используется в продажах");
        }
    }

    @Override
    public boolean isUsed(Integer id) throws Exception {
        if (id == null) {
            return false;
        }
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM sale_items WHERE discount_id = ? LIMIT 1")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void fillDiscountStatement(PreparedStatement statement, Discount discount) throws Exception {
        statement.setString(1, discount.getName());
        statement.setString(2, discount.getType().name());
        statement.setBigDecimal(3, discount.getValue());
        if (discount.getStartDate() == null) {
            statement.setNull(4, java.sql.Types.DATE);
        } else {
            statement.setDate(4, Date.valueOf(discount.getStartDate()));
        }
        if (discount.getEndDate() == null) {
            statement.setNull(5, java.sql.Types.DATE);
        } else {
            statement.setDate(5, Date.valueOf(discount.getEndDate()));
        }
        statement.setBoolean(6, discount.isActive());
    }

    private Discount mapDiscount(ResultSet resultSet) throws Exception {
        Discount discount = new Discount();
        discount.setId(resultSet.getInt("id"));
        discount.setName(resultSet.getString("name"));
        String type = resultSet.getString("type");
        discount.setType("FIXED".equalsIgnoreCase(type) ? DiscountType.FIXED : DiscountType.PERCENT);
        discount.setValue(resultSet.getBigDecimal("value"));
        Date startDate = resultSet.getDate("start_date");
        Date endDate = resultSet.getDate("end_date");
        discount.setStartDate(startDate == null ? null : startDate.toLocalDate());
        discount.setEndDate(endDate == null ? null : endDate.toLocalDate());
        discount.setActive(resultSet.getBoolean("is_active"));
        return discount;
    }
}
