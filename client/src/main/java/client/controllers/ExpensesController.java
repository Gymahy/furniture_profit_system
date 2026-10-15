package client.controllers;

import enums.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dto.DateRangeDTO;
import models.dto.DeleteRequestDTO;
import models.dto.ExpenseRequestDTO;
import models.entities.Expense;
import models.entities.ExpenseCategory;
import models.tcp.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

final class ExpensesController extends BaseController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TableView<Expense> expensesTable;
    @FXML
    private TableColumn<Expense, LocalDate> expenseDateColumn;
    @FXML
    private TableColumn<Expense, BigDecimal> amountColumn;
    @FXML
    private TableColumn<Expense, String> categoryColumn;
    @FXML
    private TableColumn<Expense, String> descriptionColumn;
    @FXML
    private DatePicker expenseDatePicker;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<ExpenseCategory> categoryBox;
    @FXML
    private TextField descriptionField;

    private final ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private final ObservableList<ExpenseCategory> categories = FXCollections.observableArrayList();

    ExpensesController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        configureColumns();
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());
        expenseDatePicker.setValue(LocalDate.now());
        categoryBox.setItems(categories);
        expensesTable.setItems(expenses);

        categories.setAll(list(RequestType.GET_EXPENSE_CATEGORIES, null));
        reloadExpenses();

        startDatePicker.valueProperty().addListener((obs, old, value) -> runSafely(this::reloadExpenses));
        endDatePicker.valueProperty().addListener((obs, old, value) -> runSafely(this::reloadExpenses));
        expensesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, expense) -> fillForm(expense));
    }

    @FXML
    private void saveExpense() {
        runSafely(() -> {
            LocalDate expenseDate = expenseDatePicker.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (!validateExpensePeriod(expenseDate, startDate, endDate)) {
                return;
            }

            Expense expense = expensesTable.getSelectionModel().getSelectedItem();
            if (expense == null) {
                expense = new Expense();
            }
            expense.setExpenseDate(expenseDate);
            expense.setAmount(parseMoney(amountField.getText()));
            expense.setCategoryId(categoryBox.getValue() == null ? null : categoryBox.getValue().getId());
            expense.setDescription(descriptionField.getText());

            Response response = request(RequestType.SAVE_EXPENSE, new ExpenseRequestDTO(expense, startDate, endDate));
            ifOk(response, this::reloadExpenses);
        });
    }

    @FXML
    private void clearExpense() {
        expensesTable.getSelectionModel().clearSelection();
        expenseDatePicker.setValue(LocalDate.now());
        amountField.clear();
        descriptionField.clear();
        categoryBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void deleteExpense() {
        Expense expense = expensesTable.getSelectionModel().getSelectedItem();
        if (expense != null) {
            ifOk(request(RequestType.DELETE_EXPENSE, new DeleteRequestDTO(expense.getId())), this::reloadExpenses);
        }
    }

    private void configureColumns() {
        expenseDateColumn.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void reloadExpenses() {
        expenses.setAll(list(RequestType.GET_EXPENSES,
                new DateRangeDTO(startDatePicker.getValue(), endDatePicker.getValue())));
    }

    private void fillForm(Expense expense) {
        if (expense == null) {
            return;
        }
        expenseDatePicker.setValue(expense.getExpenseDate());
        amountField.setText(expense.getAmount().toString());
        descriptionField.setText(expense.getDescription());
        categoryBox.getSelectionModel().select(categories.stream()
                .filter(category -> category.getId().equals(expense.getCategoryId()))
                .findFirst()
                .orElse(null));
    }

    private boolean validateExpensePeriod(LocalDate expenseDate, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            alert(Alert.AlertType.ERROR, "Укажите корректный период расходов");
            return false;
        }
        if (expenseDate == null) {
            alert(Alert.AlertType.ERROR, "Укажите дату расхода");
            return false;
        }
        if (expenseDate.isBefore(startDate) || expenseDate.isAfter(endDate)) {
            alert(Alert.AlertType.WARNING,
                    "Нельзя сохранить расход вне выбранного периода.\n"
                            + "Период: с " + formatDate(startDate) + " по " + formatDate(endDate) + ".\n"
                            + "Дата расхода: " + formatDate(expenseDate) + ".");
            return false;
        }
        return true;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMATTER.format(date);
    }
}
