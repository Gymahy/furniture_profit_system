package services;

import enums.CustomerType;
import enums.RoleType;
import models.entities.Customer;
import models.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import support.TestDatabaseSupport;

import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class CustomerServiceIntegrationTest {
    private final CustomerService service = new CustomerService();
    private Integer actorId;
    private Integer customerId;

    @AfterEach
    void cleanup() throws Exception {
        try (Connection connection = TestDatabaseSupport.connection()) {
            if (customerId != null) {
                TestDatabaseSupport.deleteCustomer(connection, customerId);
            }
            if (actorId != null) {
                TestDatabaseSupport.deleteUser(connection, actorId);
            }
        }
    }

    @Test
    void savesCustomer() throws Exception {
        setupActor();
        Customer customer = new Customer();
        customer.setFullName(TestDatabaseSupport.unique("Service Customer"));
        customer.setPhone("+375" + String.format("%09d", Math.abs(System.nanoTime()) % 1_000_000_000L));
        customer.setCustomerType(CustomerType.INDIVIDUAL);

        Customer saved = service.save(customer, actor());
        customerId = saved.getId();

        assertNotNull(saved.getRegistrationDate());
        assertTrue(!saved.getRegistrationDate().isBefore(LocalDate.now().minusDays(1)));
        assertTrue(service.findAll().stream().anyMatch(row -> customerId.equals(row.getId())));
        assertEquals(0, service.history(customerId).size());
    }

    private void setupActor() throws Exception {
        try (Connection connection = TestDatabaseSupport.connection()) {
            actorId = TestDatabaseSupport.insertUser(connection, RoleType.SALES_MANAGER, "customer_actor");
        }
    }

    private User actor() {
        User user = new User();
        user.setId(actorId);
        return user;
    }
}
