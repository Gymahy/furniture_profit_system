package models.dto;

import enums.ReportType;
import java.io.Serializable;
import java.time.LocalDate;

public class ReportRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private ReportType reportType = ReportType.PROFIT_AND_LOSS;
    private LocalDate startDate;
    private LocalDate endDate;

    public ReportRequestDTO() {
    }

    public ReportRequestDTO(ReportType reportType, LocalDate startDate, LocalDate endDate) {
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType == null ? ReportType.PROFIT_AND_LOSS : reportType;
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
}
