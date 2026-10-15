package services;

import dao.AuditDAO;
import dao.SaleDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.SaleDAOImpl;
import enums.ReportType;
import enums.SaleStatus;
import factory.FinancialOperationFactory;
import models.dto.ExportResultDTO;
import models.dto.ProfitReportDTO;
import models.dto.ReportLineDTO;
import models.dto.ReportRequestDTO;
import models.dto.SaleRequestDTO;
import models.entities.Commission;
import models.entities.Sale;
import models.entities.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SaleService {
    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();

    public Sale createSale(SaleRequestDTO request, User actor) throws Exception {
        FinancialOperationFactory.sale(request).create().validate();
        Sale sale = saleDAO.createSale(request, actor.getId());
        auditDAO.log(actor.getId(), "CREATE_SALE", "sales", sale.getId(), sale.getSaleNumber());
        return sale;
    }

    public List<Sale> findSales(LocalDate startDate, LocalDate endDate) throws Exception {
        validatePeriod(startDate, endDate);
        return saleDAO.findSales(startDate, endDate);
    }

    public void changeStatus(Integer saleId, SaleStatus status, User actor) throws Exception {
        saleDAO.changeStatus(saleId, status, null);
        auditDAO.log(actor.getId(), "CHANGE_SALE_STATUS", "sales", saleId, status.name());
    }

    public void deleteSale(Integer saleId, User actor) throws Exception {
        Sale sale = saleDAO.findSale(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Продажа не найдена"));
        saleDAO.deleteSale(saleId);
        auditDAO.log(actor.getId(), "DELETE_SALE", "sales", saleId, sale.getSaleNumber());
    }

    public ProfitReportDTO calculateProfit(LocalDate startDate, LocalDate endDate) throws Exception {
        validatePeriod(startDate, endDate);
        return saleDAO.calculateProfit(startDate, endDate);
    }

    public List<ReportLineDTO> buildReport(ReportRequestDTO request) throws Exception {
        validatePeriod(request.getStartDate(), request.getEndDate());
        return saleDAO.buildReport(request.getReportType(), request.getStartDate(), request.getEndDate());
    }

    public List<Commission> findCommissions(LocalDate startDate, LocalDate endDate) throws Exception {
        validatePeriod(startDate, endDate);
        return saleDAO.findCommissions(startDate, endDate);
    }

    public ExportResultDTO exportReport(ReportRequestDTO request, User actor) throws Exception {
        List<ReportLineDTO> lines = buildReport(request);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Тип отчета;").append(request.getReportType().getTitle()).append('\n');
        csv.append("Период;")
                .append(formatDate(request.getStartDate()))
                .append(" - ")
                .append(formatDate(request.getEndDate()))
                .append('\n');
        csv.append("Дата формирования;")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")))
                .append('\n');
        csv.append("Сформировал;").append(safe(actor == null ? "" : actor.toString())).append("\n\n");
        appendReportRows(csv, request.getReportType(), lines);
        String name = request.getReportType().name().toLowerCase() + "_"
                + request.getStartDate().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "_" + request.getEndDate().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        if (actor != null) {
            auditDAO.log(actor.getId(), "EXPORT_REPORT", "reports", null,
                    request.getReportType().getTitle() + ": "
                            + formatDate(request.getStartDate()) + " - " + formatDate(request.getEndDate()));
        }
        return new ExportResultDTO(name, csv.toString());
    }

    private void appendReportRows(StringBuilder csv, ReportType type, List<ReportLineDTO> lines) {
        switch (type) {
            case PROFIT_AND_LOSS -> {
                csv.append("Показатель;Выручка;Себестоимость;Расходы;Комиссии;Прибыль\n");
                for (ReportLineDTO line : lines) {
                    csv.append(safe(line.getGroupName())).append(';')
                            .append(decimal(line.getRevenue())).append(';')
                            .append(decimal(line.getCost())).append(';')
                            .append(decimal(line.getExpenses())).append(';')
                            .append(decimal(line.getCommissions())).append(';')
                            .append(decimal(line.getProfit())).append('\n');
                }
            }
            case PRODUCT_MARGIN -> {
                csv.append("Товар;Выручка;Себестоимость;Маржинальность, %\n");
                for (ReportLineDTO line : lines) {
                    csv.append(safe(line.getGroupName())).append(';')
                            .append(decimal(line.getRevenue())).append(';')
                            .append(decimal(line.getCost())).append(';')
                            .append(decimal(line.getMarginPercent())).append('\n');
                }
            }
            case MANAGER_COMMISSIONS -> {
                csv.append("Пользователь;Продажа;Процент комиссии;Цена расчета;Комиссия\n");
                for (ReportLineDTO line : lines) {
                    csv.append(safe(line.getGroupName())).append(';')
                            .append(safe(line.getSaleNumber())).append(';')
                            .append(decimal(line.getCommissionPercent())).append(';')
                            .append(decimal(line.getCommissionBaseAmount())).append(';')
                            .append(decimal(line.getCommissions())).append('\n');
                }
            }
            case SALES_DYNAMICS -> {
                csv.append("Дата;Количество продаж;Выручка\n");
                for (ReportLineDTO line : lines) {
                    csv.append(safe(line.getGroupName())).append(';')
                            .append(line.getQuantity()).append(';')
                            .append(decimal(line.getRevenue())).append('\n');
                }
            }
        }
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Укажите корректный отчетный период");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(";", ",");
    }

    private String decimal(BigDecimal value) {
        BigDecimal number = value == null ? BigDecimal.ZERO : value;
        return number.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
