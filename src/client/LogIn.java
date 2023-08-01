package client;

import utils.PropertiesUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class LogIn {
    JFrame frame;
    private JPanel panel;
    private JTextField loginTextField;
    private JPasswordField passwordField;
    private JButton logInButton;

    public LogIn() {
        logInButton.addActionListener(a -> {
            try(Socket socket = new Socket(
                    PropertiesUtil.getProperty("socket.ip"),
                    Integer.parseInt(PropertiesUtil.getProperty("socket.port")));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()))) {

                writer.write("LogIn");
                writer.newLine();
                writer.flush();

                writer.write(loginTextField.getText());
                writer.newLine();
                writer.flush();

                writer.write(passwordField.getPassword());
                writer.newLine();
                writer.flush();

                String answer = reader.readLine();
                if (answer.equals("AdminSuccess")) {
                    //admin rights
                } else if (answer.equals("Success")) {
                    //common user
                } else {
                    JOptionPane.showMessageDialog(null, "Wrong login or password!", "No such user!!!", JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void openFrame() {
        frame = new JFrame("YurChat");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setContentPane(panel);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        frame.setBounds(toolkit.getScreenSize().width / 2 - 325, toolkit.getScreenSize().height / 2 - 200, 550, 300);
    }

    public static void main(String[] args) {
        new LogIn().openFrame();
    }
}
