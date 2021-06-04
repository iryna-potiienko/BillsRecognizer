package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL resource = Main.class.getClassLoader().getResource("ui/main.fxml");

        assert resource != null;
        Parent root = FXMLLoader.load(resource);

        primaryStage.setTitle("РОЗПІЗНАВАЧ ЧЕКІВ СУПЕРМАРКЕТІВ");

        Scene value = new Scene(root, 420, 600);

        primaryStage.setScene(value);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
