package models.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ReportLineDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupName;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal cost = BigDecimal.ZERO;
    private BigDecimal expenses = BigDecimal.ZERO;
    private BigDecimal commissions = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal marginPercent = BigDecimal.ZERO;
    private BigDecimal commissionPercent = BigDecimal.ZERO;
    private BigDecimal commissionBaseAmount = BigDecimal.ZERO;
    private String saleNumber;
    private int quantity;

    public ReportLineDTO() {
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost == null ? BigDecimal.ZERO : cost;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses == null ? BigDecimal.ZERO : expenses;
    }

    public BigDecimal getCommissions() {
        return commissions;
    }

    public void setCommissions(BigDecimal commissions) {
        this.commissions = commissions == null ? BigDecimal.ZERO : commissions;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit == null ? BigDecimal.ZERO : profit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getMarginPercent() {
        return marginPercent;
    }

    public void setMarginPercent(BigDecimal marginPercent) {
        this.marginPercent = marginPercent == null ? BigDecimal.ZERO : marginPercent;
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

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }
}
