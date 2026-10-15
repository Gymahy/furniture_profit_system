package client.controllers;

import enums.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dto.DateRangeDTO;
import models.entities.Commission;

import java.math.BigDecimal;
import java.time.LocalDate;

final class CommissionsController extends BaseController {
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TableView<Commission> commissionsTable;
    @FXML
    private TableColumn<Commission, String> managerColumn;
    @FXML
    private TableColumn<Commission, String> saleColumn;
    @FXML
    private TableColumn<Commission, BigDecimal> percentColumn;
    @FXML
    private TableColumn<Commission, BigDecimal> baseColumn;
    @FXML
    private TableColumn<Commission, BigDecimal> amountColumn;
    @FXML
    private TableColumn<Commission, String> periodColumn;
    @FXML
    private TableColumn<Commission, String> statusColumn;

    private final ObservableList<Commission> rows = FXCollections.observableArrayList();

    CommissionsController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        managerColumn.setCellValueFactory(new PropertyValueFactory<>("managerName"));
        saleColumn.setCellValueFactory(new PropertyValueFactory<>("saleNumber"));
        percentColumn.setCellValueFactory(new PropertyValueFactory<>("commissionPercent"));
        baseColumn.setCellValueFactory(new PropertyValueFactory<>("commissionBaseAmount"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("commissionAmount"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("period"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusTitle"));

        commissionsTable.setItems(rows);
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void load() {
        rows.setAll(list(RequestType.GET_COMMISSION_REPORT,
                new DateRangeDTO(startDatePicker.getValue(), endDatePicker.getValue())));
    }
}
