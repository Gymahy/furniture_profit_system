package services.strategies;

import java.math.BigDecimal;

public interface CommissionStrategy {
    BigDecimal resolvePercent(BigDecimal saleAmount, BigDecimal margin, BigDecimal discountAmount);

    BigDecimal calculateAmount(BigDecimal margin, BigDecimal percent);
}
