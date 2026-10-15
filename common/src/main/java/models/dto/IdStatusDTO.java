package models.dto;

import enums.SaleStatus;
import java.io.Serializable;

public class IdStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private boolean active;
    private SaleStatus saleStatus;

    public IdStatusDTO() {
    }

    public IdStatusDTO(Integer id, boolean active) {
        this.id = id;
        this.active = active;
    }

    public IdStatusDTO(Integer id, SaleStatus saleStatus) {
        this.id = id;
        this.saleStatus = saleStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public SaleStatus getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(SaleStatus saleStatus) {
        this.saleStatus = saleStatus;
    }
}
