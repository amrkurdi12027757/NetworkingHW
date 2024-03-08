module org.example.networking {
  requires javafx.controls;
  requires javafx.fxml;
  requires static lombok;
  requires java.logging;

  opens org.example.networking to javafx.fxml;
  exports org.example.networking;
}