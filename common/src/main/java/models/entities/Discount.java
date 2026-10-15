package models.entities;

import enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Discount extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private DiscountType type = DiscountType.PERCENT;
    private BigDecimal value = BigDecimal.ZERO;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active = true;

    public Discount() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DiscountType getType() {
        return type;
    }

    public void setType(DiscountType type) {
        this.type = type == null ? DiscountType.PERCENT : type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value == null ? BigDecimal.ZERO : value;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isApplicable(LocalDate date) {
        LocalDate effectiveDate = date == null ? LocalDate.now() : date;
        boolean afterStart = startDate == null || !effectiveDate.isBefore(startDate);
        boolean beforeEnd = endDate == null || !effectiveDate.isAfter(endDate);
        return active && afterStart && beforeEnd;
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }
}
