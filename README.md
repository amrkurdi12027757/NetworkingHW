# Project Title: Networking Course - JavaFX Client-Server Application

## Description

This project, designed for a Networking course, demonstrates a comprehensive implementation of both client and server
functionalities using JavaFX. It includes two main components: a server window and a client window. The server is
capable of handling connections using TCP and UDP protocols and supports UDP broadcasting to assist clients in
discovering the server's dynamically allocated port numbers. The client, on the other hand, can connect to the server
using discovered port numbers and send messages either via TCP or UDP.

## Features

- Concurrent TCP and UDP server operations with dynamic port assignments.
- UDP Broadcast server for client-server discovery.
- Client interface for automatic discovery of server ports using UDP Broadcast and manual port entry if the broadcast
  port is in use.
- Interactive text areas on both server and client sides for message exchange.
- Display of server status and IP/port information for connections.

## Prerequisites

- Java Development Kit (JDK) version 11 or higher.
- Maven for dependency management and project compilation.

## Setup & Installation

1. Ensure JDK 11 (or newer) and Maven are installed on your system.
2. Clone the repository from [GitHub](https://github.com/amrkurdi12027757/NetworkingHW.git) or download the project
   files.
3. Open a terminal or command prompt and navigate to the project root directory.
4. Run `mvn clean package` to build the project. This command compiles the project and packages it into an executable
   JAR, while Maven handles dependency downloads.

## Running the Application

Follow these instructions to run the application:

1. In the project's root directory, open a terminal or command prompt.
2. Start the application with

```bash
  java -cp "./target/Networking-1.0.jar" org.example.networking.ClientsServers
  ```

3. Two windows will appear upon launch: one representing the client and the other the server.
4. The client window allows for server connection and message sending via TCP or UDP. If the UDP broadcasting port (
   9542) is in use, the client can manually enter TCP and UDP ports. The server window will display messages received
   and respond accordingly.

![alt text](https://github.com/amrkurdi12027757/NetworkingHW/blob/master/img.png?raw=true)

## How It Works

- The server employs a multi-threaded approach for handling TCP and UDP connections and runs a UDP Broadcast service for
  announcing its ports to clients.
- The client listens for UDP broadcasts for server discovery and supports manual entry of server ports if the default
  broadcasting port (9542) is occupied, ensuring connectivity under various network conditions.