package models.dto;

import enums.SaleStatus;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SaleRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer customerId;
    private Integer discountId;
    private SaleStatus initialStatus = SaleStatus.CONFIRMED;
    private List<SaleItemDTO> items = new ArrayList<>();

    public SaleRequestDTO() {
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Integer discountId) {
        this.discountId = discountId;
    }

    public SaleStatus getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(SaleStatus initialStatus) {
        this.initialStatus = initialStatus == null ? SaleStatus.CONFIRMED : initialStatus;
    }

    public List<SaleItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SaleItemDTO> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }
}
