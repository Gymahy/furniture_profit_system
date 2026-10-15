package client.controllers;

import enums.RequestType;
import enums.ResponseStatus;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import models.dto.DateRangeDTO;
import models.dto.ProfitReportDTO;
import models.tcp.Response;

import java.time.LocalDate;

final class ProfitController extends BaseController {
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private VBox metricsBox;

    ProfitController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void calculate() {
        Response response = request(RequestType.CALCULATE_PROFIT,
                new DateRangeDTO(startDatePicker.getValue(), endDatePicker.getValue()));
        if (response.getStatus() != ResponseStatus.OK) {
            return;
        }
        ProfitReportDTO report = (ProfitReportDTO) response.getData();
        metricsBox.getChildren().setAll(
                metric("Выручка, BYN", report.getRevenue().toString()),
                metric("Себестоимость, BYN", report.getCost().toString()),
                metric("Расходы, BYN", report.getExpenses().toString()),
                metric("Комиссии, BYN", report.getCommissions().toString()),
                metric("Чистая прибыль, BYN", report.getNetProfit().toString())
        );
    }

    private VBox metric(String title, String value) {
        VBox box = new VBox(6);
        box.getStyleClass().add("metric");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        valueLabel.setWrapText(true);
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }
}
