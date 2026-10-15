package enums;

import java.io.Serializable;

public enum DiscountType implements Serializable {
    PERCENT("Процентная"),
    FIXED("Фиксированная");

    private final String title;

    DiscountType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
