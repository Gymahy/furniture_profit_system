package enums;

import java.io.Serializable;

public enum CustomerType implements Serializable {
    INDIVIDUAL("Физическое лицо"),
    CORPORATE("Корпоративный клиент");

    private final String title;

    CustomerType(String title) {
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
