package enums;

import java.io.Serializable;

public enum SaleStatus implements Serializable {
    DRAFT("Черновик"),
    CONFIRMED("Подтверждена"),
    PAID("Оплачена"),
    COMPLETED("Выполнена"),
    CANCELLED("Отменена"),
    RETURNED("Возврат");

    private final String title;

    SaleStatus(String title) {
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
