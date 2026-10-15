package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 980, 640);
        scene.getStylesheets().add(ClientApp.class.getResource("/styles/app.css").toExternalForm());
        stage.setTitle("Расчет прибыли мебельного салона");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
