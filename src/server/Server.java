package server;

import utils.ConnectionManager;
import utils.PropertiesUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {
        try(ExecutorService executorService = Executors.newFixedThreadPool(10);
            ServerSocket serverSocket = new ServerSocket(
                Integer.parseInt(PropertiesUtil.getProperty("socket.port")))) {

            while (true) {
                Socket socket = serverSocket.accept();
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

            String command = "";
            while(!command.equals("exit")) {
                command = reader.readLine();

                switch (command) {
                    case "LogIn" -> logIn(writer, reader);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void logIn(BufferedWriter writer, BufferedReader reader) throws IOException {
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
                }
                else {
                    writer.write("Success");
                }
                writer.newLine();
                writer.flush();
            } else {
                writer.write("Error");
                writer.newLine();
                writer.flush();
            }

            resultSet.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}