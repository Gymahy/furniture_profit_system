package client.controllers;

import enums.CustomerType;
import enums.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dto.CustomerHistoryDTO;
import models.dto.DeleteRequestDTO;
import models.entities.Customer;

import java.math.BigDecimal;
import java.time.LocalDate;

final class CustomersController extends BaseController {
    @FXML
    private TableView<Customer> customersTable;
    @FXML
    private TableColumn<Customer, String> fullNameColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    @FXML
    private TableColumn<Customer, CustomerType> typeColumn;
    @FXML
    private TableColumn<Customer, LocalDate> registrationColumn;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<CustomerType> typeBox;
    @FXML
    private TableView<CustomerHistoryDTO> historyTable;
    @FXML
    private TableColumn<CustomerHistoryDTO, String> historyDateColumn;
    @FXML
    private TableColumn<CustomerHistoryDTO, String> historySaleNumberColumn;
    @FXML
    private TableColumn<CustomerHistoryDTO, String> historyProductsColumn;
    @FXML
    private TableColumn<CustomerHistoryDTO, String> historyStatusColumn;
    @FXML
    private TableColumn<CustomerHistoryDTO, BigDecimal> historyAmountColumn;

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final ObservableList<CustomerHistoryDTO> history = FXCollections.observableArrayList();

    CustomersController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("customerType"));
        registrationColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));

        historyDateColumn.setCellValueFactory(new PropertyValueFactory<>("saleDateFormatted"));
        historySaleNumberColumn.setCellValueFactory(new PropertyValueFactory<>("saleNumber"));
        historyProductsColumn.setCellValueFactory(new PropertyValueFactory<>("productNames"));
        historyStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        historyAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        customersTable.setItems(customers);
        historyTable.setItems(history);
        typeBox.setItems(FXCollections.observableArrayList(CustomerType.values()));
        typeBox.getSelectionModel().select(CustomerType.INDIVIDUAL);

        customersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, customer) -> fillForm(customer));

        reload();
    }

    private void reload() {
        customers.setAll(list(RequestType.GET_CUSTOMERS, null));
    }

    private void fillForm(Customer customer) {
        if (customer == null) {
            return;
        }
        nameField.setText(customer.getFullName());
        phoneField.setText(customer.getPhone());
        typeBox.getSelectionModel().select(customer.getCustomerType());
    }

    @FXML
    private void saveCustomer() {
        Customer customer = customersTable.getSelectionModel().getSelectedItem();
        if (customer == null) {
            customer = new Customer();
        }
        customer.setFullName(nameField.getText());
        customer.setPhone(phoneField.getText());
        customer.setCustomerType(typeBox.getValue());
        ifOk(request(RequestType.SAVE_CUSTOMER, customer), this::reload);
    }

    @FXML
    private void clearForm() {
        customersTable.getSelectionModel().clearSelection();
        nameField.clear();
        phoneField.clear();
        typeBox.getSelectionModel().select(CustomerType.INDIVIDUAL);
        history.clear();
    }

    @FXML
    private void deleteCustomer() {
        Customer customer = customersTable.getSelectionModel().getSelectedItem();
        if (customer != null) {
            ifOk(request(RequestType.DELETE_CUSTOMER, new DeleteRequestDTO(customer.getId())), this::reload);
        }
    }

    @FXML
    private void loadHistory() {
        Customer customer = customersTable.getSelectionModel().getSelectedItem();
        if (customer == null) {
            return;
        }
        history.setAll(list(RequestType.GET_CUSTOMER_HISTORY, new DeleteRequestDTO(customer.getId())));
    }
}
