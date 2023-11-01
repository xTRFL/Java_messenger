package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ConnectionHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket);
                clients.add(connectionHandler);
                connectionHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String clientName, String message) {
        System.out.println(clientName + ": " + message);
        for (ConnectionHandler client : clients) {
            client.sendMessage(clientName + ": " + message);
        }
    }

    private class ConnectionHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader inFromClient;
        private PrintWriter outToClient;
        private String clientName;

        public ConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outToClient = new PrintWriter(clientSocket.getOutputStream(), true);

                // Receiving client name from client
                clientName = inFromClient.readLine();
                System.out.println("Client " + clientName + " joined the chat.");

                // Sending chat history to client
                String chatHistory = getChatHistory();
                outToClient.println(chatHistory);

                String message;
                while ((message = inFromClient.readLine()) != null) {
                    System.out.println(clientName + ": " + message);
                    if (message.startsWith("/msg ")) {
                        handlePrivateMessage(clientName, message);
                    } else {
                        broadcastMessage(clientName, message);
                    }
                }

                // Client disconnected
                System.out.println("Client " + clientName + " left the chat.");
                clients.remove(this);
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            outToClient.println(message);
        }

        private void handlePrivateMessage(String sender, String message) {
            String[] temp = message.split(" ", 3);
            if (temp.length == 3) {
                String user = temp[1];
                String privateMessage = temp[2];
                for (ConnectionHandler client : clients) {
                    if (client.clientName.equals(user)) {
                        client.sendMessage("[Private] " + sender + ": " + privateMessage);
                        return;
                    }
                }
            }
        }
    }

    public String getChatHistory() {
        // Return the chat history as desired
        return "Welcome to the chat!";
    }

    public static void main(String[] args) {
        int port = 8888; // Server port

        Server server = new Server(port);
        server.start();
    }
}
