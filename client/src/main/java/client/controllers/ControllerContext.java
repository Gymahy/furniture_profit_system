package client.controllers;

import client.utility.ClientSocket;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import models.entities.User;

final class ControllerContext {
    private final ClientSocket socket;
    private final BorderPane contentPane;
    private final Label statusLabel;
    private final User currentUser;
    private Runnable contentCleanup;

    ControllerContext(ClientSocket socket, BorderPane contentPane, Label statusLabel, User currentUser) {
        this.socket = socket;
        this.contentPane = contentPane;
        this.statusLabel = statusLabel;
        this.currentUser = currentUser;
    }

    ClientSocket socket() {
        return socket;
    }

    BorderPane contentPane() {
        return contentPane;
    }

    Label statusLabel() {
        return statusLabel;
    }

    User currentUser() {
        return currentUser;
    }

    void setContent(Node node) {
        runContentCleanup();
        contentPane.setCenter(node);
    }

    void setContentCleanup(Runnable contentCleanup) {
        this.contentCleanup = contentCleanup;
    }

    void runContentCleanup() {
        if (contentCleanup != null) {
            Runnable cleanup = contentCleanup;
            contentCleanup = null;
            cleanup.run();
        }
    }
}
