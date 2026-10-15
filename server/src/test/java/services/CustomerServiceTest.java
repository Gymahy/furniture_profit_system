package services;

import models.entities.Customer;
import models.entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerServiceTest {
    private final CustomerService service = new CustomerService();

    @Test
    void rejectsBlankName() {
        Customer customer = new Customer();
        customer.setFullName(" ");
        customer.setPhone("+375291181815");

        assertThrows(IllegalArgumentException.class, () -> service.save(customer, actor()));
    }

    @Test
    void rejectsBlankPhone() {
        Customer customer = new Customer();
        customer.setFullName("Customer");
        customer.setPhone(" ");

        assertThrows(IllegalArgumentException.class, () -> service.save(customer, actor()));
    }

    @Test
    void rejectsPhoneMinus() {
        Customer customer = new Customer();
        customer.setFullName("Customer");
        customer.setPhone("-375291181815");

        assertThrows(IllegalArgumentException.class, () -> service.save(customer, actor()));
    }

    @Test
    void rejectsPhoneWithoutPlus() {
        Customer customer = new Customer();
        customer.setFullName("Customer");
        customer.setPhone("375291181815");

        assertThrows(IllegalArgumentException.class, () -> service.save(customer, actor()));
    }

    @Test
    void rejectsPhoneWithZero() {
        Customer customer = new Customer();
        customer.setFullName("Customer");
        customer.setPhone("+0375291181");

        assertThrows(IllegalArgumentException.class, () -> service.save(customer, actor()));
    }

    @Test
    void rejectsPhoneShorter7Digits() {
        Customer customer = new Customer();
        customer.setFullName("Customer");
        customer.setPhone("+123456");

        assertThrows(IllegalArgumentException.class, () -> service.save(customer, actor()));
    }

    @Test
    void rejectsPhoneLonger15() {
        Customer customer = new Customer();
        customer.setFullName("Customer");
        customer.setPhone("+1234567890123456");

        assertThrows(IllegalArgumentException.class, () -> service.save(customer, actor()));
    }

    @Test
    void rejectsWithNonDigits() {
        Customer customer = new Customer();
        customer.setFullName("Customer");
        customer.setPhone("+375 (29) 118-18-15");

        assertThrows(IllegalArgumentException.class, () -> service.save(customer, actor()));
    }

    private User actor() {
        User user = new User();
        user.setId(1);
        return user;
    }
}
