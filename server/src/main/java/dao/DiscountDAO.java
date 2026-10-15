package dao;

import models.entities.Discount;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiscountDAO extends DAO<Discount> {
    Optional<Discount> findById(Integer id) throws Exception;

    List<Discount> findActive(LocalDate date) throws Exception;

    boolean isUsed(Integer id) throws Exception;
}
