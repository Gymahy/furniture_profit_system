package dao.impl;

import dao.SaleDAO;
import enums.DiscountType;
import enums.ReportType;
import enums.SaleStatus;
import models.dto.ProfitReportDTO;
import models.dto.ReportLineDTO;
import models.dto.SaleItemDTO;
import models.dto.SaleRequestDTO;
import models.entities.Commission;
import models.entities.Discount;
import models.entities.Sale;
import models.entities.SaleItem;
import services.strategies.CommissionStrategy;
import services.strategies.DiscountStrategy;
import services.strategies.DiscountStrategyFactory;
import services.strategies.ProgressiveCommissionStrategy;
import utility.DBConnection;
import utility.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SaleDAOImpl implements SaleDAO {
    private final CatalogDAOImpl catalogDAO = new CatalogDAOImpl();
    private final CommissionStrategy commissionStrategy = new ProgressiveCommissionStrategy();

    @Override
    public Sale createSale(SaleRequestDTO request, Integer managerId) throws Exception {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Выберите клиента");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Добавьте товары в продажу");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Discount discount = request.getDiscountId() == null ? null : loadDiscount(connection, request.getDiscountId()).orElse(null);
                List<SaleItem> preparedItems = prepareItems(connection, request.getItems(), discount, shouldReserve(request.getInitialStatus()));

                Sale sale = new Sale();
                sale.setSaleNumber(generateSaleNumber());
                sale.setSaleDate(LocalDateTime.now());
                sale.setCustomerId(request.getCustomerId());
                sale.setManagerId(managerId);
                sale.setStatus(request.getInitialStatus());
                sale.setItems(preparedItems);
                sale.setTotalAmount(preparedItems.stream()
                        .map(SaleItem::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

                insertSale(connection, sale);
                for (SaleItem item : preparedItems) {
                    item.setSaleId(sale.getId());
                    insertSaleItem(connection, item);
                    if (shouldReserve(request.getInitialStatus())) {
                        catalogDAO.changeStock(connection, item.getProductId(), -item.getQuantity());
                    }
                }
                if (request.getInitialStatus() == SaleStatus.PAID || request.getInitialStatus() == SaleStatus.COMPLETED) {
                    insertCommission(connection, buildCommission(sale));
                }

                connection.commit();
                return sale;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<Sale> findSales(LocalDate startDate, LocalDate endDate) throws Exception {
        List<Sale> sales = new ArrayList<>();
        String sql = """
        SELECT s.*,
               c.full_name customer_name,
               COALESCE(NULLIF(u.full_name, ''), u.login) manager_name,
               COALESCE(si.total_amount, 0) total_amount
        FROM sales s
        LEFT JOIN customers c ON c.id = s.customer_id
        LEFT JOIN users u ON u.id = s.manager_id
        LEFT JOIN (
            SELECT sale_id, SUM(total_amount) AS total_amount
            FROM sale_items
            GROUP BY sale_id
        ) si ON si.sale_id = s.id
        WHERE s.sale_date >= ? AND s.sale_date < ?
        ORDER BY s.sale_date DESC
        """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sales.add(mapSale(resultSet));
                }
            }
        }
        return sales;
    }

    @Override
    public Optional<Sale> findSale(Integer saleId) throws Exception {
        try (Connection connection = DBConnection.getConnection()) {
            Optional<Sale> sale = loadSale(connection, saleId, false);
            sale.ifPresent(value -> {
                try {
                    value.setItems(loadItems(connection, saleId));
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            return sale;
        }
    }

    @Override
    public void changeStatus(Integer saleId, SaleStatus newStatus, Commission ignoredCommission) throws Exception {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Sale sale = loadSale(connection, saleId, true).orElseThrow(() -> new IllegalArgumentException("Продажа не найдена"));
                List<SaleItem> items = loadItems(connection, saleId);
                sale.setItems(items);
                validateTransition(sale.getStatus(), newStatus);

                if (sale.getStatus() == SaleStatus.DRAFT && newStatus == SaleStatus.CONFIRMED) {
                    for (SaleItem item : items) {
                        if (item.getProductId() != null) {
                            catalogDAO.changeStock(connection, item.getProductId(), -item.getQuantity());
                        }
                    }
                }
                if (sale.getStatus() == SaleStatus.CONFIRMED && newStatus == SaleStatus.CANCELLED) {
                    for (SaleItem item : items) {
                        if (item.getProductId() != null) {
                            catalogDAO.changeStock(connection, item.getProductId(), item.getQuantity());
                        }
                    }
                }
                if ((sale.getStatus() == SaleStatus.PAID || sale.getStatus() == SaleStatus.COMPLETED)
                        && newStatus == SaleStatus.RETURNED) {
                    for (SaleItem item : items) {
                        if (item.getProductId() != null) {
                            catalogDAO.changeStock(connection, item.getProductId(), item.getQuantity());
                        }
                    }
                    cancelCommissions(connection, saleId);
                }
                if (sale.getStatus() == SaleStatus.CONFIRMED && newStatus == SaleStatus.PAID) {
                    insertCommission(connection, buildCommission(sale));
                }
                if (sale.getStatus() == SaleStatus.PAID && newStatus == SaleStatus.COMPLETED) {
                    markCommissionsPaid(connection, saleId);
                }

                try (PreparedStatement statement = connection.prepareStatement("UPDATE sales SET status = ? WHERE id = ?")) {
                    statement.setString(1, newStatus.name());
                    statement.setInt(2, saleId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void deleteSale(Integer saleId) throws Exception {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Sale sale = loadSale(connection, saleId, true)
                        .orElseThrow(() -> new IllegalArgumentException("Продажа не найдена"));
                List<SaleItem> items = loadItems(connection, saleId);

                if (shouldReserve(sale.getStatus())) {
                    for (SaleItem item : items) {
                        if (item.getProductId() != null) {
                            catalogDAO.changeStock(connection, item.getProductId(), item.getQuantity());
                        }
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM sales WHERE id = ?")) {
                    statement.setInt(1, saleId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public ProfitReportDTO calculateProfit(LocalDate startDate, LocalDate endDate) throws Exception {
        ProfitReportDTO report = new ProfitReportDTO();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setRevenue(queryMoney("""
                SELECT COALESCE(SUM(si.total_amount), 0) value
                FROM sale_items si
                JOIN sales s ON s.id = si.sale_id
                WHERE s.status = 'COMPLETED' AND s.sale_date >= ? AND s.sale_date < ?
                """, startDate, endDate));
        report.setCost(queryMoney("""
                SELECT COALESCE(SUM(si.cost_at_sale * si.quantity), 0) value
                FROM sale_items si
                JOIN sales s ON s.id = si.sale_id
                WHERE s.status = 'COMPLETED' AND s.sale_date >= ? AND s.sale_date < ?
                """, startDate, endDate));
        report.setExpenses(queryMoney("""
                SELECT COALESCE(SUM(amount), 0) value
                FROM expenses
                WHERE expense_date BETWEEN ? AND ?
                """, startDate, endDate));
        report.setCommissions(queryMoney("""
                SELECT COALESCE(SUM(c.commission_amount), 0) value
                FROM commissions c
                JOIN sales s ON s.id = c.sale_id
                WHERE s.status = 'COMPLETED'
                  AND c.status <> 'cancelled'
                  AND s.sale_date >= ? AND s.sale_date < ?
                """, startDate, endDate));
        report.setGrossProfit(Money.amount(report.getRevenue().subtract(report.getCost())));
        report.setOperatingProfit(Money.amount(report.getGrossProfit().subtract(report.getExpenses())));
        report.setNetProfit(Money.amount(report.getOperatingProfit().subtract(report.getCommissions())));
        return report;
    }

    @Override
    public List<ReportLineDTO> buildReport(ReportType reportType, LocalDate startDate, LocalDate endDate) throws Exception {
        return switch (reportType) {
            case PRODUCT_MARGIN -> productMarginReport(startDate, endDate);
            case MANAGER_COMMISSIONS -> managerCommissionReport(startDate, endDate);
            case SALES_DYNAMICS -> salesDynamicsReport(startDate, endDate);
            case PROFIT_AND_LOSS -> profitAndLossLines(startDate, endDate);
        };
    }

    @Override
    public List<Commission> findCommissions(LocalDate startDate, LocalDate endDate) throws Exception {
        List<Commission> commissions = new ArrayList<>();
        String sql = """
                SELECT c.*,
                       s.sale_number,
                       COALESCE(NULLIF(u.full_name, ''), u.login) manager_name,
                       COALESCE(base.commission_base_amount, 0) commission_base_amount
                FROM commissions c
                LEFT JOIN users u ON u.id = c.manager_id
                JOIN sales s ON s.id = c.sale_id
                LEFT JOIN (
                    SELECT sale_id, SUM(total_amount - cost_at_sale * quantity) commission_base_amount
                    FROM sale_items
                    GROUP BY sale_id
                ) base ON base.sale_id = s.id
                WHERE s.sale_date >= ? AND s.sale_date < ?
                ORDER BY c.period DESC, manager_name
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    commissions.add(mapCommission(resultSet));
                }
            }
        }
        return commissions;
    }

    private List<SaleItem> prepareItems(Connection connection, List<SaleItemDTO> requestItems, Discount discount, boolean checkStock) throws Exception {
        List<SaleItem> items = new ArrayList<>();
        BigDecimal grossAmount = BigDecimal.ZERO;
        for (SaleItemDTO itemDTO : requestItems) {
            if (itemDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("Количество товара должно быть больше нуля");
            }
            SaleItem item = loadProductAsSaleItem(connection, itemDTO.getProductId(), itemDTO.getQuantity(), checkStock);
            items.add(item);
            grossAmount = grossAmount.add(item.getPriceAtSale().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discount != null && discount.isApplicable(LocalDate.now())) {
            DiscountStrategy strategy = DiscountStrategyFactory.forType(discount.getType());
            discountAmount = strategy.calculateDiscount(grossAmount, discount);
        }

        BigDecimal distributed = BigDecimal.ZERO;
        for (int i = 0; i < items.size(); i++) {
            SaleItem item = items.get(i);
            BigDecimal lineGross = item.getPriceAtSale().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal lineDiscount;
            if (grossAmount.compareTo(BigDecimal.ZERO) == 0) {
                lineDiscount = BigDecimal.ZERO;
            } else if (i == items.size() - 1) {
                lineDiscount = discountAmount.subtract(distributed);
            } else {
                lineDiscount = Money.amount(lineGross.multiply(discountAmount).divide(grossAmount, 6, RoundingMode.HALF_UP));
                distributed = distributed.add(lineDiscount);
            }
            item.setDiscountId(discount == null ? null : discount.getId());
            item.setDiscountName(discount == null ? null : discount.getName());
            item.setTotalAmount(Money.amount(lineGross.subtract(lineDiscount)));
        }
        return items;
    }

    private SaleItem loadProductAsSaleItem(Connection connection, Integer productId, int quantity, boolean checkStock) throws Exception {
        String sql = "SELECT id, name, stock_quantity, purchase_price, retail_price FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Товар не найден");
                }
                int stock = resultSet.getInt("stock_quantity");
                if (checkStock && stock < quantity) {
                    throw new IllegalStateException("Недостаточно товара: " + resultSet.getString("name"));
                }
                SaleItem item = new SaleItem();
                item.setProductId(productId);
                item.setProductName(resultSet.getString("name"));
                item.setQuantity(quantity);
                item.setCostAtSale(resultSet.getBigDecimal("purchase_price"));
                item.setPriceAtSale(resultSet.getBigDecimal("retail_price"));
                return item;
            }
        }
    }

    private Optional<Discount> loadDiscount(Connection connection, Integer id) throws Exception {
        String sql = "SELECT * FROM discounts WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Discount discount = new Discount();
                    discount.setId(resultSet.getInt("id"));
                    discount.setName(resultSet.getString("name"));
                    discount.setType("FIXED".equalsIgnoreCase(resultSet.getString("type")) ? DiscountType.FIXED : DiscountType.PERCENT);
                    discount.setValue(resultSet.getBigDecimal("value"));
                    Date start = resultSet.getDate("start_date");
                    Date end = resultSet.getDate("end_date");
                    discount.setStartDate(start == null ? null : start.toLocalDate());
                    discount.setEndDate(end == null ? null : end.toLocalDate());
                    discount.setActive(resultSet.getBoolean("is_active"));
                    return Optional.of(discount);
                }
            }
        }
        return Optional.empty();
    }

    private void insertSale(Connection connection, Sale sale) throws Exception {
        String sql = "INSERT INTO sales(sale_number, sale_date, customer_id, manager_id, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, sale.getSaleNumber());
            statement.setTimestamp(2, Timestamp.valueOf(sale.getSaleDate()));
            statement.setInt(3, sale.getCustomerId());
            statement.setInt(4, sale.getManagerId());
            statement.setString(5, sale.getStatus().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    sale.setId(keys.getInt(1));
                }
            }
        }
    }

    private void insertSaleItem(Connection connection, SaleItem item) throws Exception {
        String sql = """
                INSERT INTO sale_items(sale_id, product_id, quantity, price_at_sale, cost_at_sale, discount_id, total_amount)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, item.getSaleId());
            statement.setInt(2, item.getProductId());
            statement.setInt(3, item.getQuantity());
            statement.setBigDecimal(4, item.getPriceAtSale());
            statement.setBigDecimal(5, item.getCostAtSale());
            if (item.getDiscountId() == null) {
                statement.setNull(6, java.sql.Types.INTEGER);
            } else {
                statement.setInt(6, item.getDiscountId());
            }
            statement.setBigDecimal(7, item.getTotalAmount());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));
                }
            }
        }
    }

    private Optional<Sale> loadSale(Connection connection, Integer saleId, boolean forUpdate) throws Exception {
        String sql = """
                SELECT s.*,
                       c.full_name customer_name,
                       COALESCE(NULLIF(u.full_name, ''), u.login) manager_name,
                       COALESCE(SUM(si.total_amount), 0) total_amount
                FROM sales s
                LEFT JOIN customers c ON c.id = s.customer_id
                LEFT JOIN users u ON u.id = s.manager_id
                LEFT JOIN sale_items si ON si.sale_id = s.id
                WHERE s.id = ?
                GROUP BY s.id, c.full_name, u.full_name, u.login
                """ + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, saleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapSale(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    private List<SaleItem> loadItems(Connection connection, Integer saleId) throws Exception {
        List<SaleItem> items = new ArrayList<>();
        String sql = """
                SELECT si.*, p.name product_name, d.name discount_name
                FROM sale_items si
                LEFT JOIN products p ON p.id = si.product_id
                LEFT JOIN discounts d ON d.id = si.discount_id
                WHERE si.sale_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, saleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SaleItem item = new SaleItem();
                    item.setId(resultSet.getInt("id"));
                    item.setSaleId(resultSet.getInt("sale_id"));
                    int productId = resultSet.getInt("product_id");
                    item.setProductId(resultSet.wasNull() ? null : productId);
                    item.setProductName(resultSet.getString("product_name"));
                    item.setQuantity(resultSet.getInt("quantity"));
                    item.setPriceAtSale(resultSet.getBigDecimal("price_at_sale"));
                    item.setCostAtSale(resultSet.getBigDecimal("cost_at_sale"));
                    int discountId = resultSet.getInt("discount_id");
                    item.setDiscountId(resultSet.wasNull() ? null : discountId);
                    item.setDiscountName(resultSet.getString("discount_name"));
                    item.setTotalAmount(resultSet.getBigDecimal("total_amount"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private void validateTransition(SaleStatus current, SaleStatus next) {
        boolean allowed = (current == SaleStatus.DRAFT && (next == SaleStatus.CONFIRMED || next == SaleStatus.CANCELLED))
                || (current == SaleStatus.CONFIRMED && (next == SaleStatus.PAID || next == SaleStatus.CANCELLED))
                || (current == SaleStatus.PAID && (next == SaleStatus.COMPLETED || next == SaleStatus.RETURNED))
                || (current == SaleStatus.COMPLETED && next == SaleStatus.RETURNED);
        if (!allowed) {
            throw new IllegalStateException("Недопустимый переход статуса: " + current.getTitle() + " -> " + next.getTitle());
        }
    }

    private boolean shouldReserve(SaleStatus status) {
        return status == SaleStatus.CONFIRMED || status == SaleStatus.PAID || status == SaleStatus.COMPLETED;
    }

    private Commission buildCommission(Sale sale) {
        BigDecimal revenue = sale.getItems().stream().map(SaleItem::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cost = sale.getItems().stream()
                .map(item -> item.getCostAtSale().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal listAmount = sale.getItems().stream()
                .map(item -> item.getPriceAtSale().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = listAmount.subtract(revenue).max(BigDecimal.ZERO);
        BigDecimal margin = revenue.subtract(cost);
        BigDecimal percent = commissionStrategy.resolvePercent(revenue, margin, discountAmount);
        Commission commission = new Commission();
        commission.setSaleId(sale.getId());
        commission.setManagerId(sale.getManagerId());
        commission.setCommissionPercent(percent);
        commission.setCommissionBaseAmount(Money.amount(margin));
        commission.setCommissionAmount(commissionStrategy.calculateAmount(margin, percent));
        commission.setPeriod(YearMonth.from(sale.getSaleDate()).toString());
        commission.setStatus("pending");
        return commission;
    }

    private void insertCommission(Connection connection, Commission commission) throws Exception {
        String sql = """
                INSERT INTO commissions(sale_id, manager_id, commission_percent, commission_amount, period, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, commission.getSaleId());
            statement.setInt(2, commission.getManagerId());
            statement.setBigDecimal(3, commission.getCommissionPercent());
            statement.setBigDecimal(4, commission.getCommissionAmount());
            statement.setString(5, commission.getPeriod());
            statement.setString(6, commission.getStatus());
            statement.executeUpdate();
        }
    }

    private void cancelCommissions(Connection connection, Integer saleId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE commissions SET status = 'cancelled', commission_amount = 0 WHERE sale_id = ?")) {
            statement.setInt(1, saleId);
            statement.executeUpdate();
        }
    }

    private void markCommissionsPaid(Connection connection, Integer saleId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE commissions SET status = 'paid' WHERE sale_id = ? AND status = 'pending'")) {
            statement.setInt(1, saleId);
            statement.executeUpdate();
        }
    }

    private BigDecimal queryMoney(String sql, LocalDate startDate, LocalDate endDate) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (sql.contains("expense_date")) {
                statement.setDate(1, Date.valueOf(startDate));
                statement.setDate(2, Date.valueOf(endDate));
            } else {
                statement.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
                statement.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Money.amount(resultSet.getBigDecimal("value"));
                }
            }
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private List<ReportLineDTO> profitAndLossLines(LocalDate startDate, LocalDate endDate) throws Exception {
        ProfitReportDTO profit = calculateProfit(startDate, endDate);
        List<ReportLineDTO> lines = new ArrayList<>();
        lines.add(line("Выручка", profit.getRevenue(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, profit.getRevenue(), 0));
        lines.add(line("Себестоимость", BigDecimal.ZERO, profit.getCost(), BigDecimal.ZERO, BigDecimal.ZERO, profit.getCost().negate(), 0));
        lines.add(line("Операционные расходы", BigDecimal.ZERO, BigDecimal.ZERO, profit.getExpenses(), BigDecimal.ZERO, profit.getExpenses().negate(), 0));
        lines.add(line("Комиссии", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, profit.getCommissions(), profit.getCommissions().negate(), 0));
        lines.add(line("Чистая прибыль", profit.getRevenue(), profit.getCost(), profit.getExpenses(), profit.getCommissions(), profit.getNetProfit(), 0));
        return lines;
    }

    private List<ReportLineDTO> productMarginReport(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = """
                SELECT p.name group_name,
                       COALESCE(SUM(si.quantity), 0) quantity,
                       COALESCE(SUM(si.total_amount), 0) revenue,
                       COALESCE(SUM(si.cost_at_sale * si.quantity), 0) cost
                FROM sale_items si
                JOIN sales s ON s.id = si.sale_id
                LEFT JOIN products p ON p.id = si.product_id
                WHERE s.status = 'COMPLETED' AND s.sale_date >= ? AND s.sale_date < ?
                GROUP BY p.name
                ORDER BY revenue DESC
                """;
        return reportLines(sql, startDate, endDate, false);
    }

    private List<ReportLineDTO> managerCommissionReport(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = """
                SELECT COALESCE(NULLIF(u.full_name, ''), u.login) group_name,
                       s.sale_number,
                       c.commission_percent,
                       c.commission_amount commissions,
                       COALESCE(base.commission_base_amount, 0) commission_base_amount
                FROM commissions c
                JOIN sales s ON s.id = c.sale_id
                LEFT JOIN users u ON u.id = c.manager_id
                LEFT JOIN (
                    SELECT sale_id, SUM(total_amount - cost_at_sale * quantity) commission_base_amount
                    FROM sale_items
                    GROUP BY sale_id
                ) base ON base.sale_id = s.id
                WHERE c.status <> 'cancelled'
                  AND s.status = 'COMPLETED'
                  AND s.sale_date >= ? AND s.sale_date < ?
                ORDER BY s.sale_date DESC, group_name
                """;
        List<ReportLineDTO> lines = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ReportLineDTO line = new ReportLineDTO();
                    line.setGroupName(resultSet.getString("group_name"));
                    line.setSaleNumber(resultSet.getString("sale_number"));
                    line.setCommissionPercent(resultSet.getBigDecimal("commission_percent"));
                    line.setCommissionBaseAmount(resultSet.getBigDecimal("commission_base_amount"));
                    line.setCommissions(resultSet.getBigDecimal("commissions"));
                    line.setQuantity(1);
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private List<ReportLineDTO> salesDynamicsReport(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = """
                SELECT DATE(s.sale_date) group_name,
                       COUNT(DISTINCT s.id) quantity,
                       COALESCE(SUM(si.total_amount), 0) revenue,
                       COALESCE(SUM(si.cost_at_sale * si.quantity), 0) cost
                FROM sales s
                LEFT JOIN sale_items si ON si.sale_id = s.id
                WHERE s.status = 'COMPLETED' AND s.sale_date >= ? AND s.sale_date < ?
                GROUP BY DATE(s.sale_date)
                ORDER BY DATE(s.sale_date)
                """;
        return reportLines(sql, startDate, endDate, false);
    }

    private List<ReportLineDTO> reportLines(String sql, LocalDate startDate, LocalDate endDate, boolean hasCommissions) throws Exception {
        List<ReportLineDTO> lines = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ReportLineDTO line = new ReportLineDTO();
                    line.setGroupName(resultSet.getString("group_name"));
                    line.setQuantity(resultSet.getInt("quantity"));
                    line.setRevenue(resultSet.getBigDecimal("revenue"));
                    line.setCost(resultSet.getBigDecimal("cost"));
                    if (hasCommissions) {
                        line.setCommissions(resultSet.getBigDecimal("commissions"));
                    }
                    line.setProfit(Money.amount(line.getRevenue().subtract(line.getCost()).subtract(line.getCommissions())));
                    line.setMarginPercent(percent(line.getRevenue().subtract(line.getCost()), line.getRevenue()));
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private ReportLineDTO line(String name, BigDecimal revenue, BigDecimal cost, BigDecimal expenses, BigDecimal commissions, BigDecimal profit, int quantity) {
        ReportLineDTO line = new ReportLineDTO();
        line.setGroupName(name);
        line.setRevenue(revenue);
        line.setCost(cost);
        line.setExpenses(expenses);
        line.setCommissions(commissions);
        line.setProfit(profit);
        line.setQuantity(quantity);
        return line;
    }

    private Sale mapSale(ResultSet resultSet) throws Exception {
        Sale sale = new Sale();
        sale.setId(resultSet.getInt("id"));
        sale.setSaleNumber(resultSet.getString("sale_number"));
        Timestamp timestamp = resultSet.getTimestamp("sale_date");
        sale.setSaleDate(timestamp == null ? null : timestamp.toLocalDateTime());
        int customerId = resultSet.getInt("customer_id");
        sale.setCustomerId(resultSet.wasNull() ? null : customerId);
        sale.setCustomerName(resultSet.getString("customer_name"));
        int managerId = resultSet.getInt("manager_id");
        sale.setManagerId(resultSet.wasNull() ? null : managerId);
        sale.setManagerName(resultSet.getString("manager_name"));
        sale.setStatus(SaleStatus.valueOf(resultSet.getString("status")));
        sale.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        return sale;
    }

    private Commission mapCommission(ResultSet resultSet) throws Exception {
        Commission commission = new Commission();
        commission.setId(resultSet.getInt("id"));
        commission.setSaleId(resultSet.getInt("sale_id"));
        commission.setSaleNumber(resultSet.getString("sale_number"));
        commission.setManagerId(resultSet.getInt("manager_id"));
        commission.setManagerName(resultSet.getString("manager_name"));
        commission.setCommissionPercent(resultSet.getBigDecimal("commission_percent"));
        commission.setCommissionBaseAmount(resultSet.getBigDecimal("commission_base_amount"));
        commission.setCommissionAmount(resultSet.getBigDecimal("commission_amount"));
        commission.setPeriod(resultSet.getString("period"));
        commission.setStatus(resultSet.getString("status"));
        return commission;
    }

    private String generateSaleNumber() {
        return "S-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
    }

    private BigDecimal percent(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }
}
