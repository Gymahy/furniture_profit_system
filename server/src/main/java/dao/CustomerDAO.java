package dao;

import models.dto.CustomerHistoryDTO;
import models.entities.Customer;
import java.util.List;

public interface CustomerDAO extends DAO<Customer> {
    List<CustomerHistoryDTO> findHistory(Integer customerId) throws Exception;
}
