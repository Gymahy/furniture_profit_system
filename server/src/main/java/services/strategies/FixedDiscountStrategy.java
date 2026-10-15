package services.strategies;

import models.entities.Discount;
import utility.Money;

import java.math.BigDecimal;

public class FixedDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, Discount discount) {
        if (discount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = discount.getValue().min(amount);
        return Money.amount(value);
    }
}
