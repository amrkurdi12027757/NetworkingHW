package org.example.networking;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import static java.lang.Thread.sleep;

public class Clients {
  private final Button button = new Button("Connect");
  private final TextField tcpPortField;
  private final TextField udpPortField;
  private final TextField udpBroadcastPortField;
  private final TextArea tcpTextArea;
  private final TextArea udpTextArea;
  private final TextField ipAddressField;
  private final HBox container;
  private final VBox tcpBox;
  private final VBox udpBox;
  private final Button sendTcp;
  private final Button sendUdp;
  private final StringBuilder lastTcpText;
  private final StringBuilder lastUdpText;
  private Socket tcpSocket;
  private DatagramSocket udpSocket;

  /**
   * @author Amr
   * This class represents the clients of the UDP and TCP servers at the same time it receives the port numbers
   * from the UDP Broadcast Server and open the TCP and UDP sockets on those port numbers
   */
  public Clients(TextField tcpPortField,
                 TextField udpPortField,
                 TextField udpBroadcastPortField,
                 TextArea tcpTextArea,
                 TextArea udpTextArea,
                 TextField ipAddressField,
                 HBox container,
                 VBox tcpBox,
                 VBox udpBox) {


    this.tcpPortField = tcpPortField;
    this.udpPortField = udpPortField;
    this.udpBroadcastPortField = udpBroadcastPortField;
    this.tcpTextArea = tcpTextArea;
    this.udpTextArea = udpTextArea;
    this.ipAddressField = ipAddressField;
    this.container = container;
    this.tcpBox = tcpBox;
    this.udpBox = udpBox;
    lastTcpText = new StringBuilder();
    lastUdpText = new StringBuilder();

    tcpTextArea.setEditable(false);
    udpTextArea.setEditable(false);

    tcpTextArea.textProperty().addListener(new TextListener(tcpTextArea, lastTcpText));
    udpTextArea.textProperty().addListener(new TextListener(udpTextArea, lastUdpText));

    button.onMouseClickedProperty().set(event -> {
      if (button.getText().equals("Connect"))
        connect();
      else
        disconnect();
    });
    container.getChildren().add(button);

    sendTcp = new Button("Send");
    sendUdp = new Button("Send");
    sendTcp.setDisable(true);
    sendUdp.setDisable(true);
    sendTcp.onMouseClickedProperty().set(event -> sendTcp());
    sendUdp.onMouseClickedProperty().set(event -> sendUdp());
    tcpBox.getChildren().add(sendTcp);
    udpBox.getChildren().add(sendUdp);

    //UDP Broadcast Listening Thread that listens to UDP Broadcast Port 9542,
    //and it receives the IP address of the server and the TCP / UDP ports numbers
    final DatagramSocket socket;
    try {
      socket = new DatagramSocket(null);
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(9542));
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }

    byte[] receiveData = new byte[1024];
    new Thread(() -> {
      while (true) {
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
          socket.receive(receivePacket);
        } catch (IOException e) {
          throw new RuntimeException(e);
        } finally {
          socket.close();
        }
        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
        Platform.runLater(() -> {
          ipAddressField.setText(receivePacket.getAddress().getHostAddress());
          udpBroadcastPortField.setText(receivePacket.getPort() + "");
        });
        String[] split = message.split("[\n:]");
        for (int i = 0; i < split.length - 1; i += 2) {
          final int finalI = i;
          if (split[i].equals("TCP")) {
            Platform.runLater(() -> {
              tcpPortField.setText(split[finalI + 1].strip());
            });
          } else if (split[i].equals("UDP")) {
            Platform.runLater(() -> {
              udpPortField.setText(split[finalI + 1].strip());
            });
          }
        }
        Platform.runLater(() -> {
          connect();
        });
        break;
      }
    }).start();

  }

  /**
   * This method sends UDP packets to the server
   */
  private void sendUdp() {
    try {
      udpTextArea.appendText("\n");
      byte[] sendData = udpTextArea.getText().substring(lastUdpText.length()).getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAddressField.getText()), Integer.parseInt(udpPortField.getText().strip()));
      udpSocket.send(sendPacket);
      lastUdpText.append(udpTextArea.getText().substring(lastUdpText.length()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method sends TCP packets to the server
   */
  private void sendTcp() {
    try {
      tcpTextArea.appendText("\n");
      tcpSocket.getOutputStream().write((tcpTextArea.getText().substring(lastTcpText.length())).getBytes());
      lastTcpText.append(tcpTextArea.getText().substring(lastTcpText.length()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void disconnect() {

    try {
      tcpSocket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    udpSocket.close();

    button.setText("Connect");
    tcpPortField.setDisable(false);
    udpPortField.setDisable(false);
    udpBroadcastPortField.setDisable(false);
    ipAddressField.setDisable(false);
    tcpTextArea.setEditable(false);
    udpTextArea.setEditable(false);
    sendTcp.setDisable(true);
    sendUdp.setDisable(true);
  }

  private void connect() {
    if (tcpSocket != null && tcpSocket.isConnected())
      try {
        tcpSocket.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    if (udpSocket != null && udpSocket.isConnected()) {
      udpSocket.close();
    }

    try {//TCP Socket
      tcpSocket = new Socket(ipAddressField.getText().strip(), Integer.parseInt(tcpPortField.getText().strip()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try {//UDP Listening Socket
      udpSocket = new DatagramSocket(0);
      InetAddress IPAddress = InetAddress.getByName(ipAddressField.getText().strip());
      udpSocket.setReuseAddress(true);

      byte[] sendData = udpTextArea.getText().substring(lastUdpText.length()).getBytes();
      //Sending UDP packets to Server UDP Listening Port
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(udpBroadcastPortField.getText().strip()));
      udpSocket.send(sendPacket);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    new Thread(() -> {//Receiving UDP packets from Server
      while (true) {
        try {
          byte[] receiveData = new byte[1024];
          DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
          udpSocket.receive(receivePacket);
          String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
          Platform.runLater(() -> {
            udpTextArea.appendText("\nServer:\n" + message + "\n");
            lastUdpText.append("\nServer:\n").append(message).append("\n");
          });
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }).start();
    //end of Receiving UDP packets from Server

    new Thread(() -> {//Receiving TCP packets from Server
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
        String line = null;
        StringBuilder message = new StringBuilder();
        while (true) {
          if (reader.ready()) {
            while (reader.ready() && ((line = reader.readLine()) != null)) {
              message.append(line).append("\n");
            }
            final String finalMessage = message.toString();
            Platform.runLater(() -> {
              tcpTextArea.appendText("\nServer:\n" + finalMessage + "\n");
              lastTcpText.append("\nServer:\n").append(finalMessage).append("\n");
            });
            message = new StringBuilder();
          } else
            sleep(100);
        }
      } catch (IOException e) {
        try {
          tcpSocket.close();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }).start();
    //end of Receiving TCP packets from Server
    button.setText("Disconnect");
    tcpPortField.setDisable(true);
    udpPortField.setDisable(true);
    udpBroadcastPortField.setDisable(true);
    ipAddressField.setDisable(true);
    tcpTextArea.setEditable(true);
    udpTextArea.setEditable(true);
    sendTcp.setDisable(false);
    sendUdp.setDisable(false);
  }

  /**
   * This class is a listener for the text area that prevents it from overwriting the text
   * And allows it to scroll to the bottom automatically
   */
  private static class TextListener implements ChangeListener<String> {
    private final TextArea textArea;
    private final StringBuilder lastText;

    public TextListener(TextArea textArea, final StringBuilder lastText) {
      this.lastText = lastText;
      this.textArea = textArea;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
      textArea.textProperty().removeListener(this);

      try {
        if (newValue.length() < lastText.length() || !newValue.startsWith(lastText.toString())) {
          textArea.setText(oldValue);
        }
      } finally {
        textArea.setScrollTop(textArea.getLength());
        textArea.textProperty().addListener(this);
      }
    }

  }
}
