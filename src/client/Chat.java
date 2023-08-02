package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Chat{
    private JFrame frame = new JFrame();
    private JPanel mainPanel, chatPanel, interfacePanel;
    private JTextField messageTextField;
    private JButton sendButton;
    private JTextArea chatTextArea;
    private final Socket socket;
    private final String login;

    public Chat(Socket socket, String login) {
        this.socket = socket;
        this.login = login;

        Thread thread = new Thread(new ReceiveMessage());
        thread.start();

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit?", "Quit?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){

                    try(BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream()));) {
                        writer.write("Exit");
                        writer.newLine();
                        writer.flush();
                        socket.close();
                        System.exit(0);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        sendButton.addActionListener(e -> sendMessage());
    }

    public void openFrame() {
        frame = new JFrame("YurChat");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setContentPane(mainPanel);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        frame.setBounds(toolkit.getScreenSize().width / 2 - 325, toolkit.getScreenSize().height / 2 - 200, 550, 300);
    }

    private void sendMessage() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        socket.getOutputStream()))) {

            writer.write("SendMsg");
            writer.newLine();
            writer.flush();

            writer.write(login);
            writer.newLine();
            writer.flush();

            writer.write(messageTextField.getText());
            writer.newLine();
            writer.flush();

            messageTextField.setText("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ReceiveMessage implements Runnable {
        @Override
        public void run() {
            while(true) {
                try(BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()))) {
                    if(reader.ready()) {
                        String msg = reader.readLine();
                        chatTextArea.append(msg);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
