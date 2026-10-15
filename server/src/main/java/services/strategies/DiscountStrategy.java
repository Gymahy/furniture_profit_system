package services.strategies;

import models.entities.Discount;
import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal calculateDiscount(BigDecimal amount, Discount discount);
}
