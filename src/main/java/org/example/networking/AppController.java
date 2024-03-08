package org.example.networking;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AppController {
  @FXML
  public TextField tcpPortField;
  @FXML
  public TextField udpPortField;
  @FXML
  public TextField udpBroadcastPortField;
  @FXML
  public TextArea tcpTextArea;
  @FXML
  public TextArea udpTextArea;
  @FXML
  public TextField ipAddressField;
  @FXML
  public HBox container;
  @FXML
  public VBox tcpBox;
  @FXML
  public VBox udpBox;

  @FXML
  public void initialize() {

  }

  public void setServer(boolean isServer) {
    if (isServer) {
      tcpPortField.setDisable(true);
      udpPortField.setDisable(true);
      udpBroadcastPortField.setDisable(true);
      udpTextArea.setDisable(true);
      tcpTextArea.setDisable(true);
      ipAddressField.setDisable(true);
      new Servers(tcpPortField, udpPortField, udpBroadcastPortField, tcpTextArea, udpTextArea, ipAddressField);
    } else {
      udpBroadcastPortField.setDisable(true);
      new Clients(tcpPortField, udpPortField, udpBroadcastPortField, tcpTextArea, udpTextArea, ipAddressField, container, tcpBox, udpBox);
    }
  }
}