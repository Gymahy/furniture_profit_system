package client.controllers;

import client.utility.ClientSocket;
import enums.RequestType;
import enums.ResponseStatus;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;

import java.math.BigDecimal;
import java.util.List;

abstract class BaseController {
    protected final ControllerContext context;
    protected final ClientSocket socket;
    protected final User currentUser;
    protected final BorderPane contentPane;
    protected final Label statusLabel;

    protected BaseController(ControllerContext context) {
        this.context = context;
        this.socket = context.socket();
        this.currentUser = context.currentUser();
        this.contentPane = context.contentPane();
        this.statusLabel = context.statusLabel();
    }

    protected Response request(RequestType type, Object payload) {
        Response response = socket.send(new Request(type, payload));
        if (response.getStatus() == ResponseStatus.ERROR) {
            alert(Alert.AlertType.ERROR, response.getMessage());
        }
        return response;
    }

    protected Response reportRequest(RequestType type, Object payload) {
        Response response = request(type, payload);
        if (response.getStatus() == ResponseStatus.OK) {
            status(response.getMessage());
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> list(RequestType type, Object payload) {
        Response response = request(type, payload);
        if (response.getStatus() != ResponseStatus.OK || response.getData() == null) {
            return List.of();
        }
        return (List<T>) response.getData();
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> reportList(RequestType type, Object payload) {
        Response response = reportRequest(type, payload);
        if (response.getStatus() != ResponseStatus.OK || response.getData() == null) {
            return List.of();
        }
        return (List<T>) response.getData();
    }

    protected void ifOk(Response response, Runnable action) {
        if (response.getStatus() == ResponseStatus.OK) {
            action.run();
        }
    }

    protected <T> TableColumn<T, ?> col(String title, String property, int width) {
        TableColumn<T, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    protected int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Введите корректное целое число");
        }
    }

    protected BigDecimal parseMoney(String value) {
        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (Exception e) {
            throw new IllegalArgumentException("Введите корректное числовое значение");
        }
    }

    protected void runSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    protected void status(String text) {
        statusLabel.setText(text == null ? "" : text);
    }

    protected void alert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
