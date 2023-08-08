package server;

import utils.ConnectionManager;
import utils.PropertiesUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final List<Socket> clients = new ArrayList<>();

    public static void main(String[] args) {
        try(ExecutorService executorService = Executors.newFixedThreadPool(10);
            ServerSocket serverSocket = new ServerSocket(
                Integer.parseInt(PropertiesUtil.getProperty("socket.port")))) {
            System.out.println("Server is online.");
            while (true) {
                Socket socket = serverSocket.accept();
                clients.add(socket);
                executorService.execute(() -> handleClient(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleClient(Socket socket){
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream()))) {

            String command = "", login = "";
            while(!command.equals("Exit")) {
                if(reader.ready()) {
                    command = reader.readLine();
                    switch (command) {
                        case "LogIn" -> login = logIn(writer, reader);
                        case "SendMsg" -> sendMsg(socket, reader);
                        case "AddUser" -> addUser(writer, reader);
                        case "GetUsers" -> getUsers(writer);
                        case "DeleteUser" -> deleteUser(writer, reader);
                        case "Exit" -> {
                            clients.remove(socket);
                            socket.close();
                            System.out.println(login + " has disconnected");
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String logIn(BufferedWriter writer, BufferedReader reader) throws IOException {
        String login = reader.readLine();
        String password = reader.readLine();

        String sqlQuery = "SELECT * FROM users WHERE user_login = ? AND user_password = ?";

        try(var connection = ConnectionManager.getConnection();
            var preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                boolean adminAccess = resultSet.getBoolean(4);
                if(adminAccess) {
                    writer.write("AdminSuccess");
                    System.out.println(login + " has connected as admin.");
                }
                else {
                    writer.write("Success");
                    System.out.println(login + " has connected.");
                }
            } else {
                writer.write("Error");
            }
            writer.newLine();
            writer.flush();
            resultSet.close();
            return login;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMsg(Socket socket, BufferedReader reader) {
        try {
            String message = reader.readLine();

            for (Socket client : clients) {
                if (client != socket) {
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    client.getOutputStream()));
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addUser(BufferedWriter writer, BufferedReader reader) throws IOException {
        String login = reader.readLine();
        String password = reader.readLine();
        boolean isAdmin = Boolean.parseBoolean(reader.readLine());

        String sqlQuery = "INSERT INTO users (user_login, user_password, is_admin) VALUES (?, ?, ?);";

        try (var connection = ConnectionManager.getConnection();
             var preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.setBoolean(3, isAdmin);

            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                writer.write("Success");
            }
            else {
                writer.write("Error");
            }
            writer.newLine();
            writer.flush();
        } catch (SQLException e) {
            writer.write("Error");
            writer.newLine();
            writer.flush();
        }
    }

    private static void getUsers(BufferedWriter writer) throws IOException{
        String sqlQuery = "SELECT user_id, user_login, is_admin FROM users";

        try(var connection = ConnectionManager.getConnection();
            var statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sqlQuery);
            StringBuilder stringBuilder = new StringBuilder();

            while(resultSet.next()) {
                stringBuilder.append(resultSet.getInt("user_id"));
                stringBuilder.append("\t");
                stringBuilder.append(resultSet.getString("user_login"));
                stringBuilder.append("\t");
                if(resultSet.getBoolean("is_admin")) {
                    stringBuilder.append("admin");
                }
                else {
                    stringBuilder.append("user");
                }
                stringBuilder.append("|");
            }

            writer.write(stringBuilder.toString());
            writer.newLine();
            writer.flush();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteUser(BufferedWriter writer, BufferedReader reader) throws IOException {
        String userInfo = reader.readLine();

        String sqlQuery;
        if(checkIfInteger(userInfo)) {
            sqlQuery = "DELETE FROM users WHERE user_id = ?";
        }
        else {
            sqlQuery = "DELETE FROM users WHERE user_login = ?";
        }

        try(var connection = ConnectionManager.getConnection();
            var preparedStatement = connection.prepareStatement(sqlQuery)) {

            if(checkIfInteger(userInfo)) {
                preparedStatement.setInt(1, Integer.parseInt(userInfo));
            }
            else {
                preparedStatement.setString(1, userInfo);
            }

            int result = preparedStatement.executeUpdate();
            if(result > 0) {
                writer.write("Success");
            }
            else {
                writer.write("Error");
            }
            writer.newLine();
            writer.flush();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean checkIfInteger(String str) {
        try {
            Integer.parseInt(str);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
