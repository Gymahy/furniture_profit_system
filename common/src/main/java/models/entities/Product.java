package models.entities;

import java.math.BigDecimal;

public class Product extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private String article;
    private int stockQuantity;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal purchasePrice = BigDecimal.ZERO;
    private BigDecimal retailPrice = BigDecimal.ZERO;

    public Product() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice == null ? BigDecimal.ZERO : purchasePrice;
    }

    public BigDecimal getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice == null ? BigDecimal.ZERO : retailPrice;
    }

    public BigDecimal getMargin() {
        return retailPrice.subtract(purchasePrice);
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }
}
