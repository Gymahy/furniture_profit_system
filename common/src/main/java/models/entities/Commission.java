package models.entities;

import java.math.BigDecimal;

public class Commission extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer saleId;
    private String saleNumber;
    private Integer managerId;
    private String managerName;
    private BigDecimal commissionPercent = BigDecimal.ZERO;
    private BigDecimal commissionBaseAmount = BigDecimal.ZERO;
    private BigDecimal commissionAmount = BigDecimal.ZERO;
    private String period;
    private String status = "pending";

    public Commission() {
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public BigDecimal getCommissionPercent() {
        return commissionPercent;
    }

    public void setCommissionPercent(BigDecimal commissionPercent) {
        this.commissionPercent = commissionPercent == null ? BigDecimal.ZERO : commissionPercent;
    }

    public BigDecimal getCommissionBaseAmount() {
        return commissionBaseAmount;
    }

    public void setCommissionBaseAmount(BigDecimal commissionBaseAmount) {
        this.commissionBaseAmount = commissionBaseAmount == null ? BigDecimal.ZERO : commissionBaseAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount == null ? BigDecimal.ZERO : commissionAmount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusTitle() {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "pending" -> "К выплате";
            case "paid" -> "Выплачена";
            case "cancelled" -> "Отменена";
            default -> status;
        };
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
