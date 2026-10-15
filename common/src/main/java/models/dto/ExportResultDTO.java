package models.dto;

import java.io.Serializable;

public class ExportResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private String content;

    public ExportResultDTO() {
    }

    public ExportResultDTO(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
