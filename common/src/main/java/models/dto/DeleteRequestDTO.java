package models.dto;

import java.io.Serializable;

public class DeleteRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    public DeleteRequestDTO() {
    }

    public DeleteRequestDTO(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
