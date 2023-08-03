package client;

import utils.PropertiesUtil;
import utils.SocketManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LogIn {
    private JFrame frame = new JFrame();
    private JPanel panel;
    private JTextField loginTextField;
    private JPasswordField passwordField;
    private JButton logInButton;

    public LogIn() {
        logInButton.addActionListener(a -> {
            String login = loginTextField.getText();
            String password = hashPassword(Arrays.toString(passwordField.getPassword()));

            SocketManager.writeMsg("LogIn");

            SocketManager.writeMsg(login);

            SocketManager.writeMsg(password);

            String answer = SocketManager.readMsg();
            if (answer.equals("AdminSuccess")) {
                new Chat(login).openFrame();
                frame.dispose();
            } else if (answer.equals("Success")) {
                new Chat(login);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Wrong login or password!", "No such user!!!", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void openFrame() {
        frame = new JFrame("YurChat");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setContentPane(panel);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        frame.setBounds(toolkit.getScreenSize().width / 2 - 325, toolkit.getScreenSize().height / 2 - 200, 550, 300);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] hashedBytes = messageDigest.digest(password.getBytes());

            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : hashedBytes) {
                stringBuilder.append(String.format("%02x", b));
            }

            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        LogIn logIn = new LogIn();
        logIn.openFrame();
    }
}
