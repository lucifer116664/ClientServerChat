package client;

import utils.SocketManager;

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
    private final String login;

    public Chat(String login) {
        this.login = login;

        Thread thread = new Thread(new ReceiveMessage());
        thread.start();

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit?", "Quit?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    SocketManager.writeMsg("Exit");
                    SocketManager.close();
                    System.exit(0);
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
        SocketManager.writeMsg("SendMsg");
        String message = login + ":\t" + messageTextField.getText();
        SocketManager.writeMsg(message);//exception
        messageTextField.setText("");
    }

    private class ReceiveMessage implements Runnable {
        @Override
        public void run() {
            while(true) {
                String msg = SocketManager.readMsg();
                chatTextArea.append(msg);
            }
        }
    }
}
