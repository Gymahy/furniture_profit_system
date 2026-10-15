package dao;

import enums.ReportType;
import enums.SaleStatus;
import models.dto.ProfitReportDTO;
import models.dto.ReportLineDTO;
import models.dto.SaleRequestDTO;
import models.entities.Commission;
import models.entities.Sale;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleDAO {
    Sale createSale(SaleRequestDTO request, Integer managerId) throws Exception;

    List<Sale> findSales(LocalDate startDate, LocalDate endDate) throws Exception;

    Optional<Sale> findSale(Integer saleId) throws Exception;

    void changeStatus(Integer saleId, SaleStatus newStatus, Commission commission) throws Exception;

    void deleteSale(Integer saleId) throws Exception;

    ProfitReportDTO calculateProfit(LocalDate startDate, LocalDate endDate) throws Exception;

    List<ReportLineDTO> buildReport(ReportType reportType, LocalDate startDate, LocalDate endDate) throws Exception;

    List<Commission> findCommissions(LocalDate startDate, LocalDate endDate) throws Exception;
}
