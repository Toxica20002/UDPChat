package com.yinlin.udpchat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    private DatagramSocket socket;
    private byte[] buffer = new byte[1024];
    private String username;

    @FXML
    private Button enterUsernameButton;

    @FXML
    private Button sendButton;

    @FXML
    private TextArea messageText;

    @FXML
    private TextField recipientText;

    @FXML
    private TextArea textArea;

    @FXML
    private TextField usernameField;

    private void receiveFromServer() {
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                textArea.appendText(">>> " + received + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            InetAddress address = InetAddress.getByName("localhost");
            int port = 1200;
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int generateRandomPort() {
        Random random = new Random();
        return random.nextInt(65535 - 49152) + 49152;
    }


    private boolean isPortInUse(int port) {
        try (DatagramSocket ignored = new DatagramSocket(port)) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    @FXML
    void handleSendButton(ActionEvent event) {
        String recipient = recipientText.getText();
        String message = messageText.getText();
        if(recipient != null && !recipient.isEmpty() && message != null && !message.isEmpty()) {
            sendMessage(recipient + " " +  username + " " + message);
            textArea.appendText(">>> " + username + " to " + recipient + ": " + message + "\n");
            recipientText.clear();
            messageText.clear();
        }
        else{
            textArea.appendText(">>> Please enter a valid recipient and message\n");
        }
    }

    @FXML
    void handleEnterUsername(ActionEvent event) {
        username = usernameField.getText();
        if(username != null && !username.isEmpty()) {
            textArea.appendText(">>> Welcome, " + username + "!\n");
            enterUsernameButton.setDisable(true);
            sendButton.setDisable(false);
            messageText.setDisable(false);
            recipientText.setDisable(false);
            usernameField.setDisable(true);
            try {
                sendMessage(username);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            textArea.appendText(">>> Please enter a valid username\n");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textArea.setEditable(false);
        sendButton.setDisable(true);
        messageText.setDisable(true);
        recipientText.setDisable(true);

        int port = generateRandomPort();
        while (isPortInUse(port)) {
            port = generateRandomPort();
        }

        try{
            socket = new DatagramSocket(port);
            textArea.appendText(">>> Client started on port " + port + "\n");
            new Thread(this::receiveFromServer).start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
