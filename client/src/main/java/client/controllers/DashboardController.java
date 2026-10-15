package client.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import utility.RoleChecker;

final class DashboardController extends BaseController {
    @FXML
    private VBox salesCard;
    @FXML
    private VBox customersCard;
    @FXML
    private VBox financeCard;
    @FXML
    private VBox adminCard;

    DashboardController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        setCardVisible(salesCard, RoleChecker.canWorkWithSales(currentUser));
        setCardVisible(customersCard, RoleChecker.canWorkWithSales(currentUser));
        setCardVisible(financeCard, RoleChecker.canWorkWithFinance(currentUser));
        setCardVisible(adminCard, RoleChecker.canManageUsers(currentUser));
    }

    private void setCardVisible(VBox card, boolean visible) {
        card.setVisible(visible);
        card.setManaged(visible);
    }
}
