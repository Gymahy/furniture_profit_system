package services.strategies;

import utility.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProgressiveCommissionStrategy implements CommissionStrategy {
    @Override
    public BigDecimal resolvePercent(BigDecimal saleAmount, BigDecimal margin, BigDecimal discountAmount) {
        BigDecimal percent = BigDecimal.valueOf(3);
        if (saleAmount.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            percent = BigDecimal.valueOf(7);
        } else if (saleAmount.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            percent = BigDecimal.valueOf(5);
        }

        if (discountAmount.compareTo(saleAmount.multiply(BigDecimal.valueOf(0.15))) > 0) {
            percent = percent.subtract(BigDecimal.ONE);
        }
        return percent.max(BigDecimal.valueOf(2));
    }

    @Override
    public BigDecimal calculateAmount(BigDecimal margin, BigDecimal percent) {
        if (margin.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return Money.amount(margin.multiply(percent).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
    }
}
