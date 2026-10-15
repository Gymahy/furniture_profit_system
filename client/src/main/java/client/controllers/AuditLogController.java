package client.controllers;

import enums.RequestType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import models.entities.AuditLog;

final class AuditLogController extends BaseController {
    @FXML
    private TableView<AuditLog> auditTable;
    @FXML
    private TableColumn<AuditLog, String> timeColumn;
    @FXML
    private TableColumn<AuditLog, String> userColumn;
    @FXML
    private TableColumn<AuditLog, String> actionColumn;
    @FXML
    private TableColumn<AuditLog, String> entityTypeColumn;
    @FXML
    private TableColumn<AuditLog, Integer> entityIdColumn;
    @FXML
    private TableColumn<AuditLog, String> detailsColumn;

    private final ObservableList<AuditLog> rows = FXCollections.observableArrayList();
    private Timeline refreshTimeline;

    AuditLogController(ControllerContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtFormatted"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("actionTitle"));
        entityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityIdColumn.setCellValueFactory(new PropertyValueFactory<>("entityId"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));

        auditTable.setItems(rows);
        reload();

        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> reload()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
        context.setContentCleanup(this::stopRefresh);
    }

    private void reload() {
        rows.setAll(list(RequestType.GET_AUDIT_LOGS, null));
    }

    private void stopRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
    }
}
