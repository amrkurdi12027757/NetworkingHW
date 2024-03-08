package org.example.networking;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientsServers extends Application {
  @Override
  public void start(Stage clientStage) throws IOException {
    clientStage.setTitle("Client-Side");
    FXMLLoader clientFxmlLoader = new FXMLLoader((getClass().getResource("interface.fxml")));
    Parent root = clientFxmlLoader.load();
    AppController controller = clientFxmlLoader.getController();
    controller.setServer(false);
    Scene scene = new Scene(root);
    clientStage.setScene(scene);
    clientStage.show();

    Stage serverStage = new Stage();
    serverStage.setX(clientStage.getX() + clientStage.getWidth());
    serverStage.setY(clientStage.getY());
    serverStage.setTitle("Server-Side");
    FXMLLoader serverFxmlLoader = new FXMLLoader((getClass().getResource("interface.fxml")));
    root = serverFxmlLoader.load();
    controller = serverFxmlLoader.getController();
    controller.setServer(true);
    scene = new Scene(root);
    serverStage.setScene(scene);
    serverStage.show();

  }

  public static void main(String[] args) {
    launch();
  }
}