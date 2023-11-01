package Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client extends JFrame {
    private List<String> blockedUsers;
    private String serverAddress;
    private int serverPort;
    private String clientName;
    private JTextArea chatArea;
    private JTextField messageField;
    private Socket clientSocket;
    private PrintWriter outToServer;

    public Client(String serverAddress, int serverPort, String clientName) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.clientName = clientName;
        this.blockedUsers = new ArrayList<>();

        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 300));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                sendMessage(message);
            }
        });
        add(messageField, BorderLayout.SOUTH);
    }
    public String getName()
    {
        return this.clientName;
    }

    public void start() {
        pack();
        setVisible(true);

        try {
            clientSocket = new Socket(serverAddress, serverPort);
            outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Sending client name to the server
            outToServer.println(clientName);

            // Receiving chat history from the server
            String chatHistory = inFromServer.readLine();
            appendMessage("Chat History:\n" + chatHistory);

            // Creating a thread to receive messages from the server
            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = inFromServer.readLine()) != null) {
                        appendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        messageField.setText("");

        if (message.startsWith("/")) {
            handleCommand(message);
        } else {
            outToServer.println(message);
        }
    }

    private void handleCommand(String command) {
        if (command.startsWith("/block")) {
            String[] temp = command.split(" ");
            if (temp.length >= 2) {
                String userToBlock = temp[1];
                blockedUsers.add(userToBlock);
                appendMessage("You have blocked user: " + userToBlock);
            } else {
                appendMessage("There is no user specified");
            }
        } else if (command.startsWith("/unblock")) {
            String[] temp = command.split(" ");
            if (temp.length >= 2) {
                String userToUnblock = temp[1];
                blockedUsers.remove(userToUnblock);
                appendMessage("You have unblocked user: " + userToUnblock);
            }
        } else if (command.startsWith("/help")) {
            appendMessage("/help\n"
                    + "/clear\n"
                    + "/block\n"
                    + "/unblock\n");
        } else if (command.equals("/clear")) {
            chatArea.setText("");
        } else if (command.startsWith("/msg")) {
            String[] temp = command.split(" ", 3);
            if (temp.length >= 3) {
                String recipient = temp[1];
                String privateMessage = temp[2];
                outToServer.println("/msg " + recipient + " " + privateMessage);
                appendMessage("(Private message sent to " + recipient + "): " + privateMessage);
            } else {
            }
        } else {
            appendMessage("Invalid command: " + command);
        }
    }

    private void appendMessage(String message) {
        String[] temp = message.split(":");
        String sender = temp[0].trim();
        if (blockedUsers.contains(sender)) {
            return;
        }
        chatArea.append(message + "\n");
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Server address
        int serverPort = 8888; // Server port

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter client name: ");
        String clientName = scanner.nextLine();

        Client client = new Client(serverAddress, serverPort, clientName);
        client.start();
    }
}
