package services;

import enums.ReportType;
import models.dto.ReportRequestDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SaleServiceTest {
    private final SaleService service = new SaleService();

    @Test
    void rejectsInvalidSalesPeriod() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findSales(LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 1)));
    }

    @Test
    void rejectsMissingProfitPeriod() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateProfit(null, LocalDate.of(2026, 5, 1)));
    }

    @Test
    void rejectsInvalidReportPeriod() {
        ReportRequestDTO request = new ReportRequestDTO(
                ReportType.PROFIT_AND_LOSS,
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 5, 1));

        assertThrows(IllegalArgumentException.class, () -> service.buildReport(request));
    }
}
