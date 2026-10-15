package services.strategies;

import enums.DiscountType;

public final class DiscountStrategyFactory {
    private static final DiscountStrategy PERCENTAGE = new PercentageDiscountStrategy();
    private static final DiscountStrategy FIXED = new FixedDiscountStrategy();

    private DiscountStrategyFactory() {
    }

    public static DiscountStrategy forType(DiscountType type) {
        return type == DiscountType.FIXED ? FIXED : PERCENTAGE;
    }
}
