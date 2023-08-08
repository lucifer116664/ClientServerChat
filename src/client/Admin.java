package client;

import utils.SocketManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.util.Arrays;

public class Admin extends Chat{
    private JTabbedPane tabbedPane;
    private JPanel mainPanel;
    private JButton addUserButton, reloadButton, deleteButton;
    private JTextField loginTextField, deleteTextField;
    private JPasswordField passwordField;
    private JCheckBox adminAccessCheckBox;
    private JTextArea usersTextArea;
    private JFrame frame;

    public Admin(String login) {
        super(login);
        loadUsers();

        addUserButton.addActionListener(e -> addUser());
        reloadButton.addActionListener(e -> loadUsers());
        deleteButton.addActionListener(e -> deleteUser());
    }

    @Override
    public void openFrame() {
        frame = new JFrame("YurChat");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
        frame.setContentPane(mainPanel);
        tabbedPane.add("Chat", chatMainPanel);

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

    private void addUser() {
        String prohibitedSymbols = "`~!@#$%^&*()-_+={}[]\\|/?.,<>;:'\"";
        if(loginTextField.getText().isEmpty() || Arrays.toString(passwordField.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "You must enter login and password.",
                    "Login or password field is empty",
                    JOptionPane.WARNING_MESSAGE);
        } else if (loginTextField.getText().contains(prohibitedSymbols)) {
            JOptionPane.showMessageDialog(null,
                    "You can not use this symbols in login text field: " + prohibitedSymbols,
                    "Login field contains prohibited symbol(s)!!!",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            SocketManager.writeMsg("AddUser");
            SocketManager.writeMsg(loginTextField.getText());
            String password = LogIn.hashPassword(Arrays.toString(passwordField.getPassword()));
            SocketManager.writeMsg(password);
            if(adminAccessCheckBox.isSelected()) {
                SocketManager.writeMsg("true");
            }
            else {
                SocketManager.writeMsg("false");
            }

            String answer = SocketManager.readMsg();

            if(answer.equals("Success")) {
                JOptionPane.showMessageDialog(null,
                        "User was successfully added.",
                        "Success!!!",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(null,
                        "User with this login already exists.",
                        "Error!!!",
                        JOptionPane.ERROR_MESSAGE);
            }
            loginTextField.setText("");
            passwordField.setText("");
            adminAccessCheckBox.setSelected(false);
            loadUsers();
        }
    }

    private void loadUsers () {
        usersTextArea.setText("");
        usersTextArea.append("ID\tLogin\tRights\n");

        SocketManager.writeMsg("GetUsers");
        String users = SocketManager.readMsg();
        users = users.replace('|', '\n');

        usersTextArea.append(users);
    }

    private void deleteUser() {
        if (!deleteTextField.getText().isEmpty()) {
            SocketManager.writeMsg("DeleteUser");

            SocketManager.writeMsg(deleteTextField.getText());
            String result = SocketManager.readMsg();
            if (result.equals("Success")) {
                JOptionPane.showMessageDialog(null,
                        "User was successfully deleted.",
                        "Success!!!",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Error occurred while deleting user.",
                        "Error!!!",
                        JOptionPane.ERROR_MESSAGE);
            }
            deleteTextField.setText("");
            loadUsers();
        }
        else {
            JOptionPane.showMessageDialog(null,
                    "Enter user login or id.",
                    "Empty text field!!!",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
