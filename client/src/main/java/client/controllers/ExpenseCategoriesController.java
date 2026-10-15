package client.controllers;

import enums.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dto.DeleteRequestDTO;
import models.entities.ExpenseCategory;

final class ExpenseCategoriesController extends BaseController {
    @FXML
    private TableView<ExpenseCategory> categoriesTable;
    @FXML
    private TableColumn<ExpenseCategory, String> nameColumn;
    @FXML
    private TableColumn<ExpenseCategory, String> descriptionColumn;
    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;

    private final ObservableList<ExpenseCategory> categories = FXCollections.observableArrayList();

    ExpenseCategoriesController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        categoriesTable.setItems(categories);
        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, category) -> fillForm(category));

        reload();
    }

    private void reload() {
        categories.setAll(list(RequestType.GET_EXPENSE_CATEGORIES, null));
    }

    private void fillForm(ExpenseCategory category) {
        if (category == null) {
            return;
        }
        nameField.setText(category.getName());
        descriptionField.setText(category.getDescription());
    }

    @FXML
    private void saveCategory() {
        runSafely(() -> {
            ExpenseCategory category = categoriesTable.getSelectionModel().getSelectedItem();
            if (category == null) {
                category = new ExpenseCategory();
            }
            category.setName(nameField.getText());
            category.setDescription(descriptionField.getText());
            ifOk(request(RequestType.SAVE_EXPENSE_CATEGORY, category), this::reload);
        });
    }

    @FXML
    private void clearForm() {
        categoriesTable.getSelectionModel().clearSelection();
        nameField.clear();
        descriptionField.clear();
    }

    @FXML
    private void deleteCategory() {
        ExpenseCategory category = categoriesTable.getSelectionModel().getSelectedItem();
        if (category != null) {
            ifOk(request(RequestType.DELETE_EXPENSE_CATEGORY, new DeleteRequestDTO(category.getId())), this::reload);
        }
    }
}
