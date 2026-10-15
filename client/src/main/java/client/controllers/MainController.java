package client.controllers;

import client.utility.ClientSocket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.entities.User;
import utility.RoleChecker;

import java.util.function.Supplier;

public class MainController {
    @FXML
    private VBox menuBox;
    @FXML
    private BorderPane contentPane;
    @FXML
    private Label userLabel;
    @FXML
    private Label statusLabel;

    private final ClientSocket socket = ClientSocket.getInstance();

    private User currentUser;
    private ControllerContext context;

    public void initializeUser(User user) {
        this.currentUser = user;
        this.context = new ControllerContext(socket, contentPane, statusLabel, user);

        userLabel.setText(user + " | " + user.getRoleTitle());
        buildMenu();
        loadScreen("/fxml/dashboard.fxml", () -> new DashboardController(context));
    }

    private void buildMenu() {
        menuBox.getChildren().clear();
        if (RoleChecker.canWorkWithSales(currentUser)) {
            menuBox.getChildren().add(menuButton("\uD83D\uDED2 Продажи", () -> loadScreen("/fxml/sales.fxml", () -> new SalesScreenController(context))));
            menuBox.getChildren().add(menuButton("\uD83D\uDC65 Клиенты", () -> loadScreen("/fxml/customers.fxml", () -> new CustomersController(context))));
        }
        menuBox.getChildren().add(menuButton("\uD83D\uDCE6 Товары и остатки", () -> loadScreen("/fxml/products.fxml", () -> new ProductsController(context))));
        if (RoleChecker.canWorkWithFinance(currentUser)) {
            menuBox.getChildren().add(menuButton("\uD83D\uDCC9 Расходы", () -> loadScreen("/fxml/expenses.fxml", () -> new ExpensesController(context))));
            menuBox.getChildren().add(menuButton("\uD83D\uDCC1 Категории расходов", () -> loadScreen("/fxml/expense-categories.fxml", () -> new ExpenseCategoriesController(context))));
            menuBox.getChildren().add(menuButton("\uD83D\uDCB0 Расчет прибыли", () -> loadScreen("/fxml/profit.fxml", () -> new ProfitController(context))));
            menuBox.getChildren().add(menuButton("\uD83D\uDCCA Отчеты", () -> loadScreen("/fxml/reports.fxml", () -> new ReportsController(context))));
            menuBox.getChildren().add(menuButton("\uD83D\uDCBC Комиссии", () -> loadScreen("/fxml/commissions.fxml", () -> new CommissionsController(context))));
        }
        if (RoleChecker.canManageCatalog(currentUser)) {
            menuBox.getChildren().add(menuButton("\uD83D\uDCC2 Категории товаров", () -> loadScreen("/fxml/categories.fxml", () -> new CategoriesController(context))));
        }
        if (RoleChecker.canManageDiscounts(currentUser)) {
            menuBox.getChildren().add(menuButton("% Скидки", () -> loadScreen("/fxml/discounts.fxml", () -> new DiscountsController(context))));
        }
        if (RoleChecker.canManageUsers(currentUser)) {
            menuBox.getChildren().add(menuButton("\uD83D\uDC64 Пользователи", () -> loadScreen("/fxml/users.fxml", () -> new UsersController(context))));
            menuBox.getChildren().add(menuButton("\uD83D\uDCDC История логов", () -> loadScreen("/fxml/audit.fxml", () -> new AuditLogController(context))));
        }
        Button logout = menuButton("← Выйти ко входу", this::logout);
        VBox.setVgrow(logout, Priority.NEVER);
        menuBox.getChildren().add(logout);
    }

    private void loadScreen(String fxml, Supplier<? extends BaseController> factory) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setController(factory.get());
            Parent root = loader.load();
            context.setContent(root);
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Не удалось открыть экран: " + e.getMessage());
        }
    }

    private Button menuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void logout() {
        try {
            if (context != null) {
                context.runContentCleanup();
            }
            socket.disconnect();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Расчет прибыли мебельного салона");
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Не удалось вернуться ко входу: " + e.getMessage());
        }
    }

    private void alert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
