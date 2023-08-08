package client;

import utils.SocketManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;

public class Chat{
    private JFrame frame;
    protected JPanel chatMainPanel, chatPanel, interfacePanel;
    private JTextField messageTextField;
    private JButton sendButton;
    private JTextArea chatTextArea;
    protected final String login;
    protected boolean runMessageReceiver;

    public Chat(String login) {
        this.login = login;

        Thread messageReceiver = new Thread(new MessageReceiver());
        runMessageReceiver = true;
        messageReceiver.start();

        sendButton.addActionListener(e -> sendMessage());
    }

    public void openFrame() {
        frame = new JFrame("YurChat");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
        frame.setContentPane(chatMainPanel);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        frame.setBounds(toolkit.getScreenSize().width / 2 - 325, toolkit.getScreenSize().height / 2 - 200, 650, 400);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int dialogResult = JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit?", "Quit?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if ( dialogResult== JOptionPane.YES_OPTION) {
                    runMessageReceiver = false;
                    SocketManager.writeMsg("Exit");
                    SocketManager.close();
                    System.exit(0);
                }
            }
        });
    }

    private void sendMessage() {
        SocketManager.writeMsg("SendMsg");
        String message = login + ":\t" + messageTextField.getText();
        SocketManager.writeMsg(message);
        messageTextField.setText("");
        chatTextArea.append(message + "\n");
    }

    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            while(runMessageReceiver) {
                if(SocketManager.isReaderReady()) {
                    String msg = SocketManager.readMsg();
                    chatTextArea.append(msg + "\n");
                }
            }
        }
    }
}
