package client.controllers;

import enums.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.dto.DeleteRequestDTO;
import models.entities.Category;
import models.entities.Product;
import utility.RoleChecker;

import java.math.BigDecimal;

final class ProductsController extends BaseController {
    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> articleColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, BigDecimal> purchaseColumn;
    @FXML
    private TableColumn<Product, BigDecimal> retailColumn;
    @FXML
    private Label sideTitle;
    @FXML
    private GridPane formGrid;
    @FXML
    private TextField nameField;
    @FXML
    private TextField articleField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField purchaseField;
    @FXML
    private TextField retailField;
    @FXML
    private ComboBox<Category> categoryBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;

    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    ProductsController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        articleColumn.setCellValueFactory(new PropertyValueFactory<>("article"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        purchaseColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        retailColumn.setCellValueFactory(new PropertyValueFactory<>("retailPrice"));

        productsTable.setItems(products);
        categoryBox.setItems(categories);

        boolean editable = RoleChecker.canManageCatalog(currentUser);
        if (!editable) {
            sideTitle.setText("Просмотр товара");
            formGrid.setDisable(true);
            saveButton.setDisable(true);
            deleteButton.setDisable(true);
        }

        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, product) -> fillForm(product));

        reload();
    }

    private void reload() {
        products.setAll(list(RequestType.GET_PRODUCTS, null));
        categories.setAll(list(RequestType.GET_CATEGORIES, null));
    }

    private void fillForm(Product product) {
        if (product == null) {
            return;
        }
        nameField.setText(product.getName());
        articleField.setText(product.getArticle());
        stockField.setText(String.valueOf(product.getStockQuantity()));
        purchaseField.setText(product.getPurchasePrice().toString());
        retailField.setText(product.getRetailPrice().toString());
        categoryBox.getSelectionModel().select(categories.stream()
                .filter(c -> c.getId().equals(product.getCategoryId()))
                .findFirst().orElse(null));
    }

    @FXML
    private void saveProduct() {
        runSafely(() -> {
            Product product = productsTable.getSelectionModel().getSelectedItem();
            if (product == null) {
                product = new Product();
            }
            product.setName(nameField.getText());
            product.setArticle(articleField.getText());
            product.setStockQuantity(parseInt(stockField.getText()));
            product.setPurchasePrice(parseMoney(purchaseField.getText()));
            product.setRetailPrice(parseMoney(retailField.getText()));
            product.setCategoryId(categoryBox.getValue() == null ? null : categoryBox.getValue().getId());
            ifOk(request(RequestType.SAVE_PRODUCT, product), this::reload);
        });
    }

    @FXML
    private void clearForm() {
        productsTable.getSelectionModel().clearSelection();
        nameField.clear();
        articleField.clear();
        stockField.clear();
        purchaseField.clear();
        retailField.clear();
        categoryBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void deleteProduct() {
        Product product = productsTable.getSelectionModel().getSelectedItem();
        if (product != null) {
            ifOk(request(RequestType.DELETE_PRODUCT, new DeleteRequestDTO(product.getId())), this::reload);
        }
    }
}
