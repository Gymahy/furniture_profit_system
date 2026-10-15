package models.entities;

import enums.CustomerType;
import java.time.LocalDate;

public class Customer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private String phone;
    private CustomerType customerType = CustomerType.INDIVIDUAL;
    private LocalDate registrationDate;

    public Customer() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType == null ? CustomerType.INDIVIDUAL : customerType;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return fullName == null ? "" : fullName;
    }
}
