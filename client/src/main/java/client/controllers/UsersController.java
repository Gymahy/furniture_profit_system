package client.controllers;

import enums.RequestType;
import enums.RoleType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dto.DeleteRequestDTO;
import models.dto.IdStatusDTO;
import models.dto.PasswordResetDTO;
import models.entities.Role;
import models.entities.User;

final class UsersController extends BaseController {
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> loginColumn;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, Boolean> activeColumn;
    @FXML
    private TextField loginField;
    @FXML
    private TextField fullNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<Role> roleBox;

    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final ObservableList<Role> roles = FXCollections.observableArrayList();

    UsersController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleTitle"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        usersTable.setItems(users);
        roleBox.setItems(roles);

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, user) -> fillForm(user));

        reload();
    }

    private void reload() {
        users.setAll(list(RequestType.GET_USERS, null));
        roles.setAll(list(RequestType.GET_ROLES, null));
    }

    private void fillForm(User user) {
        if (user == null) {
            roleBox.setDisable(false);
            return;
        }
        loginField.setText(user.getLogin());
        fullNameField.setText(user.getFullName());
        passwordField.clear();
        roleBox.getSelectionModel().select(roles.stream()
                .filter(r -> r.getName() == user.getRole())
                .findFirst().orElse(null));
        boolean protectedRole = user.getRole() == RoleType.ADMIN
                || currentUser != null && user.getId() != null && user.getId().equals(currentUser.getId());
        roleBox.setDisable(protectedRole);
    }

    @FXML
    private void saveUser() {
        runSafely(() -> {
            User user = usersTable.getSelectionModel().getSelectedItem();
            if (user == null) {
                user = new User();
                user.setPasswordHash(passwordField.getText());
                user.setActive(true);
            }
            user.setLogin(loginField.getText());
            user.setFullName(fullNameField.getText());
            Role selectedRole = roleBox.getValue();
            user.setRoleId(selectedRole == null ? null : selectedRole.getId());
            user.setRole(selectedRole == null ? RoleType.SALES_MANAGER : selectedRole.getName());
            ifOk(request(RequestType.SAVE_USER, user), this::reload);
        });
    }

    @FXML
    private void clearForm() {
        usersTable.getSelectionModel().clearSelection();
        loginField.clear();
        fullNameField.clear();
        passwordField.clear();
        roleBox.getSelectionModel().clearSelection();
        roleBox.setDisable(false);
    }

    @FXML
    private void toggleActive() {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user != null) {
            ifOk(request(RequestType.SET_USER_ACTIVE, new IdStatusDTO(user.getId(), !user.isActive())), this::reload);
        }
    }

    @FXML
    private void resetPassword() {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user != null && !passwordField.getText().isBlank()) {
            ifOk(request(RequestType.RESET_PASSWORD, new PasswordResetDTO(user.getId(), passwordField.getText())), this::reload);
        }
    }

    @FXML
    private void deleteUser() {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user != null) {
            ifOk(request(RequestType.DELETE_USER, new DeleteRequestDTO(user.getId())), this::reload);
        }
    }
}
