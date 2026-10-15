package client.controllers;

import client.utility.ClientSocket;
import enums.RequestType;
import enums.ResponseStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.dto.LoginDTO;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;

public class LoginController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    @FXML
    private void login() {
        statusLabel.setText("");
        if (loginField.getText().isBlank() || passwordField.getText().isBlank()) {
            statusLabel.setText("Введите логин и пароль.");
            return;
        }
        ClientSocket socket = ClientSocket.getInstance();
        if (!socket.isConnected() && !socket.connect()) {
            statusLabel.setText("Сервер недоступен. Запустите server.ServerApp.");
            return;
        }

        Response response = socket.send(new Request(RequestType.LOGIN, new LoginDTO(loginField.getText(), passwordField.getText())));
        if (response.getStatus() != ResponseStatus.OK) {
            statusLabel.setText(response.getMessage());
            return;
        }

        User user = (User) response.getData();
        socket.setCurrentUser(user);
        openMain(user);
    }

    private void openMain(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.initializeUser(user);
            Stage stage = (Stage) loginField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Расчет прибыли мебельного салона - " + user.getRoleTitle());
        } catch (Exception e) {
            statusLabel.setText("Не удалось открыть главное окно: " + e.getMessage());
        }
    }
}
