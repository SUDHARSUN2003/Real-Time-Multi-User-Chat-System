import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class ChatServer {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Chat server started on port 5000...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clientHandlers) {
            client.sendMessage(message);
        }
    }

    public static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Ask username
            out.println("Enter your username:");
            username = in.readLine();

            // Send recent chat messages
            sendChatHistory(out);

            // Notify all
            ChatServer.broadcast(username + " joined the chat!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                String msgWithTime = "[" + new java.util.Date() + "] " + username + ": " + message;
                System.out.println(msgWithTime);
                ChatServer.broadcast(msgWithTime);
                saveMessageToDB(username, message);
            }
        } catch (IOException e) {
            System.out.println(username + " disconnected.");
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeClient(this);
            ChatServer.broadcast(username + " left the chat!");
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void sendChatHistory(PrintWriter out) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT sender, message, timestamp FROM messages ORDER BY id DESC LIMIT 10"
            );
            ResultSet rs = stmt.executeQuery();

            Stack<String> messages = new Stack<>();
            while (rs.next()) {
                String msg = "[" + rs.getTimestamp("timestamp") + "] " +
                             rs.getString("sender") + ": " +
                             rs.getString("message");
                messages.push(msg);
            }

            out.println("\n--- Recent Chat Messages ---");
            while (!messages.isEmpty()) {
                out.println(messages.pop());
            }
            out.println("-----------------------------\n");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveMessageToDB(String sender, String message) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO messages(sender, message) VALUES(?, ?)");
            stmt.setString(1, sender);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
