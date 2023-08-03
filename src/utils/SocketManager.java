package utils;

import java.io.*;
import java.net.Socket;

public final class SocketManager {
    private static final Socket socket;
    private static final BufferedWriter writer;
    private static final BufferedReader reader;

    static {
        try {
            socket = new Socket(
                    PropertiesUtil.getProperty("socket.ip"),
                    Integer.parseInt(PropertiesUtil.getProperty("socket.port")));
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            socket.getOutputStream()));
            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeMsg(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readMsg() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void close() {
        try {
            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
