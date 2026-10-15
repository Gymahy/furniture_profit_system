package services.strategies;

import models.entities.Discount;
import utility.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, Discount discount) {
        if (discount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal percent = discount.getValue().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return Money.amount(amount.multiply(percent));
    }
}
