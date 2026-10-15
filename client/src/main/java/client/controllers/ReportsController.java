package client.controllers;

import enums.ReportType;
import enums.RequestType;
import enums.ResponseStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.dto.ExportResultDTO;
import models.dto.ReportLineDTO;
import models.dto.ReportRequestDTO;
import models.tcp.Response;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

final class ReportsController extends BaseController {
    @FXML
    private ComboBox<ReportType> reportTypeBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private VBox reportBody;
    @FXML
    private TableView<ReportLineDTO> reportTable;

    private final ObservableList<ReportLineDTO> rows = FXCollections.observableArrayList();

    ReportsController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());
        reportTypeBox.setItems(FXCollections.observableArrayList(ReportType.values()));
        reportTypeBox.getSelectionModel().select(ReportType.PROFIT_AND_LOSS);
        reportTable.setItems(rows);
        VBox.setVgrow(reportTable, Priority.ALWAYS);
        VBox.setVgrow(reportBody, Priority.ALWAYS);
        configureReportColumns(reportTable, reportTypeBox.getValue());

        reportTypeBox.valueProperty().addListener((obs, old, selected) -> {
            rows.clear();
            configureReportColumns(reportTable, selected);
            reportBody.getChildren().setAll(reportTable);
        });
    }

    @FXML
    private void buildReport() {
        ReportType selectedType = reportTypeBox.getValue();
        rows.setAll(reportList(RequestType.GET_FINANCIAL_REPORT,
                new ReportRequestDTO(selectedType, startDatePicker.getValue(), endDatePicker.getValue())));
        configureReportColumns(reportTable, selectedType);
        if (selectedType == ReportType.SALES_DYNAMICS) {
            reportBody.getChildren().setAll(buildSalesChart(rows), reportTable);
        } else {
            reportBody.getChildren().setAll(reportTable);
        }
    }

    @FXML
    private void exportReport() {
        Response response = reportRequest(RequestType.EXPORT_REPORT,
                new ReportRequestDTO(reportTypeBox.getValue(), startDatePicker.getValue(), endDatePicker.getValue()));
        if (response.getStatus() == ResponseStatus.OK) {
            saveExport((ExportResultDTO) response.getData());
        }
    }

    private void configureReportColumns(TableView<ReportLineDTO> table, ReportType reportType) {
        table.getColumns().clear();
        switch (reportType) {
            case PROFIT_AND_LOSS -> table.getColumns().addAll(
                    col("Показатель", "groupName", 220),
                    col("Выручка", "revenue", 110),
                    col("Себестоимость", "cost", 120),
                    col("Расходы", "expenses", 110),
                    col("Комиссии", "commissions", 110),
                    col("Прибыль", "profit", 120)
            );
            case PRODUCT_MARGIN -> table.getColumns().addAll(
                    col("Товар", "groupName", 260),
                    col("Выручка", "revenue", 120),
                    col("Себестоимость", "cost", 130),
                    col("Маржинальность, %", "marginPercent", 150)
            );
            case MANAGER_COMMISSIONS -> table.getColumns().addAll(
                    col("Пользователь", "groupName", 190),
                    col("Продажа", "saleNumber", 130),
                    col("Процент комиссии", "commissionPercent", 140),
                    col("Цена расчета", "commissionBaseAmount", 130),
                    col("Комиссия", "commissions", 110)
            );
            case SALES_DYNAMICS -> table.getColumns().addAll(
                    col("Дата", "groupName", 140),
                    col("Количество продаж", "quantity", 150),
                    col("Выручка", "revenue", 130)
            );
        }
    }

    private LineChart<String, Number> buildSalesChart(List<ReportLineDTO> rows) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Дата");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Выручка, BYN");
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Динамика продаж за период");
        chart.setLegendVisible(false);
        chart.setMinHeight(280);
        chart.getStyleClass().add("report-chart");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ReportLineDTO row : rows) {
            series.getData().add(new XYChart.Data<>(row.getGroupName(), row.getRevenue()));
        }
        chart.getData().add(series);
        return chart;
    }

    private void saveExport(ExportResultDTO export) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(export.getFileName());
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File file = chooser.showSaveDialog(contentPane.getScene().getWindow());
            if (file != null) {
                Files.writeString(file.toPath(), export.getContent(), StandardCharsets.UTF_8);
                status("Отчет успешно создан: " + file.getAbsolutePath());
                alert(Alert.AlertType.INFORMATION, "Отчет успешно создан:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }
}
