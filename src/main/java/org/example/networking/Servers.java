package org.example.networking;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

/**
 * @author Amr
 * This class represents three servers at a time.
 * First server will accept connections from clients on TCP
 * Second server will accept connections from clients on UDP
 * Third server will act only as a UDP Broadcast server
 * because the other two servers will always run on any empty port
 * so if the client doesn't know the port number of the server it will
 * be sent to him/her and will immediately connect to the server
 * *
 */
@Log
public class Servers {
  /**
   * This regex is used to split the text into characters
   */
  private static final String ZERO_WIDTH_SPLITTER = "(?=)(?<=)";
  /**
   * This regex is used to split the text on numbers (delete the numbers from the text)
   */
  private static final String NUMBER_SPLITTER = "[0-9]+";
  /**
   * Regex for checking if the text is numeric
   */
  private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
  /**
   * Regex for checking if the text is alphabetic
   */
  private static final Pattern ALPHA_PATTERN = Pattern.compile("^[a-zA-Z\\s]+");

  /**
   * States of the servers
   */
  private enum STATES {

    CLIENT_CONNECTED,
    CLIENT_DISCONNECTED,
    SERVER_STARTED,
    SERVER_STOPPED,
    SERVER_ERROR,
    BROADCASTING_STARTED,
    BROADCASTING_STOPPED,
    BROADCASTING_ERROR;

    /**
     * This enum represents the types of servers
     */
    private enum TYPE {
      TCP,
      UDP
    }

    private final static HashMap<STATES, String> STATES_MAP = new HashMap<>() {{
      put(CLIENT_CONNECTED, "Client Connected");
      put(CLIENT_DISCONNECTED, "Client Disconnected");
      put(SERVER_STARTED, "Server Started");
      put(SERVER_STOPPED, "Server Stopped");
      put(SERVER_ERROR, "Server Error");
      put(BROADCASTING_STARTED, "Broadcasting Started");
      put(BROADCASTING_STOPPED, "Broadcasting Stopped");
      put(BROADCASTING_ERROR, "Broadcasting Error");
    }};

    public static String get(TYPE type, STATES state, int port) {
      return type + " " + STATES_MAP.get(state) + " @ " + port;
    }
  }

  /**
   * TCP Server Socket
   */
  private static ServerSocket tcpServerSocket;
  /**
   * UDP Server Socket
   */
  private static DatagramSocket udpServerSocket;

  public Servers(TextField tcpPortField, TextField udpPortField, TextField udpBroadcastPortField, TextArea tcpTextArea, TextArea udpTextArea, TextField ipAddressField) {

    tcpTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
      tcpTextArea.setScrollTop(tcpTextArea.getLength());
    });
    udpTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
      udpTextArea.setScrollTop(udpTextArea.getLength());
    });

    //BROADCASTING SERVER
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    executorService.scheduleAtFixedRate(() -> {
      try (DatagramSocket socket = new DatagramSocket(null)) {
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(9542));
        log.info(STATES.get(STATES.TYPE.UDP, STATES.BROADCASTING_STARTED, socket.getLocalPort()));
        Platform.runLater(() -> {
          udpBroadcastPortField.setText(socket.getLocalPort() + "");
        });
        socket.setBroadcast(true);
        if (tcpServerSocket == null || udpServerSocket == null) {
          log.info(STATES.get(STATES.TYPE.UDP, STATES.BROADCASTING_STOPPED, socket.getLocalPort()));
          return;
        }
        String message = "TCP:" + tcpServerSocket.getLocalPort() + "\nUDP:" + udpServerSocket.getLocalPort();
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), socket.getLocalPort());
        socket.send(packet);
        log.info(STATES.get(STATES.TYPE.UDP, STATES.BROADCASTING_STOPPED, socket.getLocalPort()));
      } catch (IOException e) {
        log.severe(STATES.get(STATES.TYPE.UDP, STATES.BROADCASTING_ERROR, 0));
        throw new RuntimeException(e);
      }

    }, 2, 10, TimeUnit.SECONDS);


    //TCP /UDP SERVER

    Thread tcpThread = new Thread(() -> {
      try {
        tcpServerSocket = new ServerSocket(0);
        tcpServerSocket.setReuseAddress(true);
        log.info(STATES.get(STATES.TYPE.TCP, STATES.SERVER_STARTED, tcpServerSocket.getLocalPort()));
        Platform.runLater(() -> {
          tcpPortField.setText(tcpServerSocket.getLocalPort() + "");
          try {
            ipAddressField.setText(InetAddress.getLocalHost().getHostAddress());
          } catch (UnknownHostException e) {
            throw new RuntimeException(e);
          }
        });
        while (true) {
          Socket socket = tcpServerSocket.accept();
          socket.setReuseAddress(true);
          log.info(STATES.get(STATES.TYPE.TCP, STATES.CLIENT_CONNECTED, tcpServerSocket.getLocalPort()));

          new Thread(() -> {

            if (socket.isConnected()) {
              try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = null;
                StringBuilder message = new StringBuilder();
                while (true) {
                  if (reader.ready()) {
                    while (reader.ready() && ((line = reader.readLine()) != null)) {
                      message.append(line).append(reader.ready() ? "\n" : "");
                    }
                    final String finalMessage = message.toString();
                    log.info("TCP Received: " + finalMessage);
                    Platform.runLater(() -> {
                      tcpTextArea.appendText("\nClient:\n" + finalMessage + "\n");
                    });
                    String response = getResponse(finalMessage.strip());
                    socket.getOutputStream().write((response + "\n").getBytes());
                    socket.getOutputStream().flush();
                    log.info("TCP Sent: " + response);
                    Platform.runLater(() -> {
                      tcpTextArea.appendText("\nServer:\n" + response + "\n");
                    });
                    message = new StringBuilder();
                  } else
                    sleep(100);
                }
              } catch (IOException e) {
                log.severe(STATES.get(STATES.TYPE.TCP, STATES.SERVER_ERROR, tcpServerSocket.getLocalPort()));
                try {
                  socket.close();
                } catch (IOException ex) {
                  log.severe(STATES.get(STATES.TYPE.TCP, STATES.SERVER_ERROR, tcpServerSocket.getLocalPort()));
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
              }
            }
          }).start();

        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        try {
          tcpServerSocket.close();
          log.info(STATES.get(STATES.TYPE.TCP, STATES.SERVER_STOPPED, tcpServerSocket.getLocalPort()));
        } catch (IOException e) {
          log.severe(STATES.get(STATES.TYPE.TCP, STATES.SERVER_ERROR, tcpServerSocket.getLocalPort()));
        }
      }
    });


    Thread udpThread = new Thread(() -> {
      DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
      try {
        udpServerSocket = new DatagramSocket(0);
        udpServerSocket.setReuseAddress(true);
        log.info(STATES.get(STATES.TYPE.UDP, STATES.SERVER_STARTED, udpServerSocket.getLocalPort()));
        Platform.runLater(() -> {
          udpPortField.setText(udpServerSocket.getLocalPort() + "");
        });
        while (true) {
          udpServerSocket.receive(datagramPacket);
          log.info(STATES.get(STATES.TYPE.UDP, STATES.CLIENT_CONNECTED, udpServerSocket.getLocalPort()));

          String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
          log.info("UDP Received:\n" + message);
          Platform.runLater(() -> {
            udpTextArea.appendText("\nClient:\n" + message + "\n");
          });
          String response = getResponse(message.strip());
          byte[] sendData = response.getBytes();
          DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, datagramPacket.getAddress(), datagramPacket.getPort());
          udpServerSocket.send(sendPacket);
          Platform.runLater(() -> {
            udpTextArea.appendText("\nServer:\n" + response + "\n");
          });
        }
      } catch (IOException e) {
        log.severe(STATES.get(STATES.TYPE.UDP, STATES.SERVER_ERROR, udpServerSocket.getLocalPort()));
        throw new RuntimeException(e);
      } finally {
        log.info(STATES.get(STATES.TYPE.UDP, STATES.SERVER_STOPPED, udpServerSocket.getLocalPort()));
        udpServerSocket.close();
      }
    });

    tcpThread.start();
    udpThread.start();
  }

  private String getResponse(String message) {
    if (ALPHA_PATTERN.matcher(message).matches()) {
      return new StringBuilder(message).reverse().toString().toUpperCase();
    } else if (NUMERIC_PATTERN.matcher(message).matches()) {
      {
        String[] split = message.split(ZERO_WIDTH_SPLITTER);
        int sum = 0;
        for (int i = 0; i < split.length; i++) {
          sum += Integer.parseInt(split[i]);
        }
        return String.valueOf(sum);
      }
    }
    String[] split = message.split(NUMBER_SPLITTER);
    return String.join("", split);

  }


}