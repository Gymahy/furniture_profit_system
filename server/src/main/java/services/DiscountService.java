package services;

import dao.AuditDAO;
import dao.DiscountDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.DiscountDAOImpl;
import models.entities.Discount;
import models.entities.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DiscountService {
    private final DiscountDAO discountDAO = new DiscountDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();

    public List<Discount> findAll() throws Exception {
        return discountDAO.findAll();
    }

    public List<Discount> findActive() throws Exception {
        return discountDAO.findActive(LocalDate.now());
    }

    public Discount save(Discount discount, User actor) throws Exception {
        if (discount.getName() == null || discount.getName().isBlank()) {
            throw new IllegalArgumentException("Укажите название скидки");
        }
        if (discount.getType() == null) {
            throw new IllegalArgumentException("Укажите тип скидки");
        }
        if (discount.getValue() == null) {
            throw new IllegalArgumentException("Укажите размер скидки");
        }
        if (discount.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Размер скидки не может быть отрицательным");
        }
        if (discount.getStartDate() != null && discount.getEndDate() != null && discount.getStartDate().isAfter(discount.getEndDate())) {
            throw new IllegalArgumentException("Дата начала скидки позже даты окончания");
        }
        Discount saved = discountDAO.save(discount);
        auditDAO.log(actor.getId(), "SAVE_DISCOUNT", "discounts", saved.getId(), saved.getName());
        return saved;
    }

    public void delete(Integer id, User actor) throws Exception {
        if (discountDAO.isUsed(id)) {
            throw new IllegalArgumentException("Нельзя удалить скидку: она используется в продажах");
        }
        discountDAO.delete(id);
        auditDAO.log(actor.getId(), "DELETE_DISCOUNT", "discounts", id, null);
    }
}
