package models.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SaleItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;

    public SaleItemDTO() {
    }

    public SaleItemDTO(Integer productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
