import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Scanner;

public class ChatClient {

    private static String username;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.println("Enter username:");
            username = scanner.nextLine();
            loggedIn = loginOrRegister(username, scanner);
        }

        System.out.println("Welcome, " + username + "! Connecting to chat...");

        try (Socket socket = new Socket("SERVER_IP_HERE", 5000); // Replace with Railway server IP/URL
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Read messages from server
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            // Send messages
            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("/quit")) {
                    System.out.println("Exiting chat...");
                    break;
                }
                out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean loginOrRegister(String username, Scanner sc) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement checkUser = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ?");
            checkUser.setString(1, username);
            ResultSet rs = checkUser.executeQuery();

            if (rs.next()) {
                System.out.println("Enter password:");
                String pass = sc.nextLine();
                if (pass.equals(rs.getString("password"))) {
                    System.out.println("Login successful!");
                    return true;
                } else {
                    System.out.println("Incorrect password!");
                    return false;
                }
            } else {
                System.out.println("New user! Set your password:");
                String pass = sc.nextLine();
                PreparedStatement insertUser = conn.prepareStatement(
                    "INSERT INTO users(username, password) VALUES(?, ?)");
                insertUser.setString(1, username);
                insertUser.setString(2, pass);
                insertUser.executeUpdate();
                System.out.println("Registration successful!");
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
