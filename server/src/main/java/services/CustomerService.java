package services;

import dao.AuditDAO;
import dao.CustomerDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.CustomerDAOImpl;
import models.dto.CustomerHistoryDTO;
import models.entities.Customer;
import models.entities.User;

import java.time.LocalDate;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();

    public List<Customer> findAll() throws Exception {
        return customerDAO.findAll();
    }

    public Customer save(Customer customer, User actor) throws Exception {
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw new IllegalArgumentException("Укажите имя клиента");
        }
        if (customer.getPhone() == null || customer.getPhone().isBlank()) {
            throw new IllegalArgumentException("Укажите телефон клиента");
        }
        if (!customer.getPhone().matches("\\+[1-9]\\d{6,14}")) {
            throw new IllegalArgumentException("Телефон должен начинаться со знака плюс, первая цифра не может быть нулём, всего от 7 до 15 цифр");
        }
        if (customer.getRegistrationDate() == null) {
            customer.setRegistrationDate(LocalDate.now());
        }
        Customer saved = customerDAO.save(customer);
        auditDAO.log(actor.getId(), "SAVE_CUSTOMER", "customers", saved.getId(), saved.getFullName());
        return saved;
    }

    public void delete(Integer id, User actor) throws Exception {
        customerDAO.delete(id);
        auditDAO.log(actor.getId(), "DELETE_CUSTOMER", "customers", id, null);
    }

    public List<CustomerHistoryDTO> history(Integer customerId) throws Exception {
        return customerDAO.findHistory(customerId);
    }
}
