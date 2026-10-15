package dao.impl;

import dao.CustomerDAO;
import enums.CustomerType;
import enums.SaleStatus;
import models.dto.CustomerHistoryDTO;
import models.entities.Customer;
import utility.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {
    @Override
    public List<Customer> findAll() throws Exception {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY full_name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                customers.add(mapCustomer(resultSet));
            }
        }
        return customers;
    }

    @Override
    public Customer save(Customer entity) throws Exception {
        if (entity.isNew()) {
            String sql = "INSERT INTO customers(full_name, phone, customer_type, registration_date) VALUES (?, ?, ?, ?)";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillCustomerStatement(statement, entity);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        entity.setId(keys.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE customers SET full_name = ?, phone = ?, customer_type = ?, registration_date = ? WHERE id = ?";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                fillCustomerStatement(statement, entity);
                statement.setInt(5, entity.getId());
                statement.executeUpdate();
            }
        }
        return entity;
    }

    @Override
    public void delete(Integer id) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM customers WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public List<CustomerHistoryDTO> findHistory(Integer customerId) throws Exception {
        List<CustomerHistoryDTO> history = new ArrayList<>();
        String sql = """
                SELECT s.sale_number,
                       s.sale_date,
                       s.status,
                       GROUP_CONCAT(CONCAT(p.name, ' (', si.quantity, ' шт.)') ORDER BY p.name SEPARATOR ', ') product_names,
                       COALESCE(SUM(si.total_amount), 0) amount
                FROM sales s
                LEFT JOIN sale_items si ON si.sale_id = s.id
                LEFT JOIN products p ON p.id = si.product_id
                WHERE s.customer_id = ?
                GROUP BY s.id, s.sale_number, s.sale_date, s.status
                ORDER BY s.sale_date DESC
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    CustomerHistoryDTO dto = new CustomerHistoryDTO();
                    dto.setSaleNumber(resultSet.getString("sale_number"));
                    Timestamp timestamp = resultSet.getTimestamp("sale_date");
                    dto.setSaleDate(timestamp == null ? null : timestamp.toLocalDateTime());
                    dto.setProductNames(resultSet.getString("product_names"));
                    dto.setStatus(SaleStatus.valueOf(resultSet.getString("status")).getTitle());
                    dto.setAmount(resultSet.getBigDecimal("amount"));
                    history.add(dto);
                }
            }
        }
        return history;
    }

    private void fillCustomerStatement(PreparedStatement statement, Customer customer) throws Exception {
        statement.setString(1, customer.getFullName());
        statement.setString(2, customer.getPhone());
        statement.setString(3, customer.getCustomerType() == CustomerType.CORPORATE ? "corporate" : "individual");
        if (customer.getRegistrationDate() == null) {
            statement.setDate(4, Date.valueOf(java.time.LocalDate.now()));
        } else {
            statement.setDate(4, Date.valueOf(customer.getRegistrationDate()));
        }
    }

    private Customer mapCustomer(ResultSet resultSet) throws Exception {
        Customer customer = new Customer();
        customer.setId(resultSet.getInt("id"));
        customer.setFullName(resultSet.getString("full_name"));
        customer.setPhone(resultSet.getString("phone"));
        customer.setCustomerType("corporate".equalsIgnoreCase(resultSet.getString("customer_type"))
                ? CustomerType.CORPORATE
                : CustomerType.INDIVIDUAL);
        Date registrationDate = resultSet.getDate("registration_date");
        customer.setRegistrationDate(registrationDate == null ? null : registrationDate.toLocalDate());
        return customer;
    }
}
