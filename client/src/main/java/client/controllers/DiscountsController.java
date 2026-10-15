package client.controllers;

import enums.DiscountType;
import enums.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dto.DeleteRequestDTO;
import models.entities.Discount;

import java.math.BigDecimal;
import java.time.LocalDate;

final class DiscountsController extends BaseController {
    @FXML
    private TableView<Discount> discountsTable;
    @FXML
    private TableColumn<Discount, String> nameColumn;
    @FXML
    private TableColumn<Discount, DiscountType> typeColumn;
    @FXML
    private TableColumn<Discount, BigDecimal> valueColumn;
    @FXML
    private TableColumn<Discount, LocalDate> startColumn;
    @FXML
    private TableColumn<Discount, LocalDate> endColumn;
    @FXML
    private TableColumn<Discount, Boolean> activeColumn;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<DiscountType> typeBox;
    @FXML
    private TextField valueField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    private final ObservableList<Discount> discounts = FXCollections.observableArrayList();

    DiscountsController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        discountsTable.setItems(discounts);
        typeBox.setItems(FXCollections.observableArrayList(DiscountType.values()));
        typeBox.getSelectionModel().select(DiscountType.PERCENT);
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusMonths(1));

        discountsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, discount) -> fillForm(discount));

        reload();
    }

    private void reload() {
        discounts.setAll(list(RequestType.GET_DISCOUNTS, null));
    }

    private void fillForm(Discount discount) {
        if (discount == null) {
            return;
        }
        nameField.setText(discount.getName());
        typeBox.getSelectionModel().select(discount.getType());
        valueField.setText(discount.getValue().toString());
        startDatePicker.setValue(discount.getStartDate());
        endDatePicker.setValue(discount.getEndDate());
    }

    @FXML
    private void saveDiscount() {
        runSafely(() -> {
            Discount discount = discountsTable.getSelectionModel().getSelectedItem();
            if (discount == null) {
                discount = new Discount();
            }
            discount.setName(nameField.getText());
            discount.setType(typeBox.getValue());
            discount.setValue(parseMoney(valueField.getText()));
            discount.setStartDate(startDatePicker.getValue());
            discount.setEndDate(endDatePicker.getValue());
            discount.setActive(true);
            ifOk(request(RequestType.SAVE_DISCOUNT, discount), this::reload);
        });
    }

    @FXML
    private void clearForm() {
        discountsTable.getSelectionModel().clearSelection();
        nameField.clear();
        valueField.clear();
        typeBox.getSelectionModel().select(DiscountType.PERCENT);
    }

    @FXML
    private void deleteDiscount() {
        Discount discount = discountsTable.getSelectionModel().getSelectedItem();
        if (discount != null) {
            ifOk(request(RequestType.DELETE_DISCOUNT, new DeleteRequestDTO(discount.getId())), this::reload);
        }
    }
}
