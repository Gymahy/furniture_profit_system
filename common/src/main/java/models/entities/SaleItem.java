package models.entities;

import java.math.BigDecimal;

public class SaleItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer saleId;
    private Integer productId;
    private String productName;
    private int quantity;
    private BigDecimal priceAtSale = BigDecimal.ZERO;
    private BigDecimal costAtSale = BigDecimal.ZERO;
    private Integer discountId;
    private String discountName;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public SaleItem() {
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtSale() {
        return priceAtSale;
    }

    public void setPriceAtSale(BigDecimal priceAtSale) {
        this.priceAtSale = priceAtSale == null ? BigDecimal.ZERO : priceAtSale;
    }

    public BigDecimal getCostAtSale() {
        return costAtSale;
    }

    public void setCostAtSale(BigDecimal costAtSale) {
        this.costAtSale = costAtSale == null ? BigDecimal.ZERO : costAtSale;
    }

    public Integer getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Integer discountId) {
        this.discountId = discountId;
    }

    public String getDiscountName() {
        return discountName;
    }

    public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }
}
