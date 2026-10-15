package client.controllers;

import enums.RequestType;
import enums.SaleStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dto.DateRangeDTO;
import models.dto.DeleteRequestDTO;
import models.dto.IdStatusDTO;
import models.dto.SaleItemDTO;
import models.dto.SaleRequestDTO;
import models.dto.StockCheckDTO;
import models.entities.Customer;
import models.entities.Discount;
import models.entities.Product;
import models.entities.Sale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

final class SalesScreenController extends BaseController {
    @FXML
    private ComboBox<Customer> customerBox;
    @FXML
    private ComboBox<Product> productBox;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private ComboBox<Discount> discountBox;
    @FXML
    private TableView<SaleItemDTO> cartTable;
    @FXML
    private TableColumn<SaleItemDTO, String> cartProductColumn;
    @FXML
    private TableColumn<SaleItemDTO, Integer> cartQuantityColumn;
    @FXML
    private TableColumn<SaleItemDTO, BigDecimal> cartPriceColumn;
    @FXML
    private TableColumn<SaleItemDTO, BigDecimal> cartTotalColumn;
    @FXML
    private TableView<Sale> salesTable;
    @FXML
    private TableColumn<Sale, String> saleNumberColumn;
    @FXML
    private TableColumn<Sale, String> saleDateColumn;
    @FXML
    private TableColumn<Sale, String> saleCustomerColumn;
    @FXML
    private TableColumn<Sale, String> saleManagerColumn;
    @FXML
    private TableColumn<Sale, SaleStatus> saleStatusColumn;
    @FXML
    private TableColumn<Sale, BigDecimal> saleAmountColumn;
    @FXML
    private ComboBox<SaleStatus> statusBox;

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ObservableList<Discount> discounts = FXCollections.observableArrayList();
    private final ObservableList<SaleItemDTO> cart = FXCollections.observableArrayList();
    private final ObservableList<Sale> sales = FXCollections.observableArrayList();

    SalesScreenController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        cartProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        cartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        cartTotalColumn.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        saleNumberColumn.setCellValueFactory(new PropertyValueFactory<>("saleNumber"));
        saleDateColumn.setCellValueFactory(new PropertyValueFactory<>("saleDateFormatted"));
        saleCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        saleManagerColumn.setCellValueFactory(new PropertyValueFactory<>("managerName"));
        saleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        saleAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        customerBox.setItems(customers);
        productBox.setItems(products);
        discountBox.setItems(discounts);
        cartTable.setItems(cart);
        salesTable.setItems(sales);
        statusBox.setItems(FXCollections.observableArrayList(SaleStatus.values()));
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        reload();
    }

    private void reload() {
        customers.setAll(list(RequestType.GET_CUSTOMERS, null));
        products.setAll(list(RequestType.GET_PRODUCTS, null));
        discounts.setAll(list(RequestType.GET_DISCOUNTS, null));
        sales.setAll(loadSalesForMonth());
    }

    private List<Sale> loadSalesForMonth() {
        return list(RequestType.GET_SALES, new DateRangeDTO(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
    }

    @FXML
    private void checkStock() {
        Product product = productBox.getValue();
        if (product == null) {
            return;
        }
        StockCheckDTO result = (StockCheckDTO) request(RequestType.CHECK_STOCK,
                new StockCheckDTO(product.getId(), quantitySpinner.getValue())).getData();
        if (result != null) {
            alert(result.isAvailable() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING,
                    result.getProductName() + ": доступно " + result.getStockQuantity());
        }
    }

    @FXML
    private void addItem() {
        Product product = productBox.getValue();
        if (product == null) {
            alert(Alert.AlertType.WARNING, "Необходимо выбрать товар");
            return;
        }
        cart.add(new SaleItemDTO(product.getId(), product.getName(),
                quantitySpinner.getValue(), product.getRetailPrice()));
    }

    @FXML
    private void removeItem() {
        cart.remove(cartTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void createSale() {
        submitSale(SaleStatus.CONFIRMED);
    }

    @FXML
    private void saveDraft() {
        submitSale(SaleStatus.DRAFT);
    }

    private void submitSale(SaleStatus initialStatus) {
        SaleRequestDTO saleRequest = new SaleRequestDTO();
        saleRequest.setCustomerId(customerBox.getValue() == null ? null : customerBox.getValue().getId());
        saleRequest.setDiscountId(discountBox.getValue() == null ? null : discountBox.getValue().getId());
        saleRequest.setInitialStatus(initialStatus);
        saleRequest.setItems(List.copyOf(cart));
        ifOk(request(RequestType.CREATE_SALE, saleRequest), () -> {
            cart.clear();
            reload();
        });
    }

    @FXML
    private void changeStatus() {
        Sale sale = salesTable.getSelectionModel().getSelectedItem();
        if (sale != null && statusBox.getValue() != null) {
            ifOk(request(RequestType.CHANGE_SALE_STATUS, new IdStatusDTO(sale.getId(), statusBox.getValue())), this::reload);
        }
    }

    @FXML
    private void deleteSale() {
        Sale sale = salesTable.getSelectionModel().getSelectedItem();
        if (sale != null) {
            ifOk(request(RequestType.DELETE_SALE, new DeleteRequestDTO(sale.getId())), this::reload);
        }
    }
}
