package enums;

import java.io.Serializable;

public enum ReportType implements Serializable {
    PROFIT_AND_LOSS("Отчет о прибылях и убытках"),
    PRODUCT_MARGIN("Маржинальность товаров"),
    MANAGER_COMMISSIONS("Ведомость комиссий"),
    SALES_DYNAMICS("Динамика продаж");

    private final String title;

    ReportType(String title) {
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
