package com.yinlin.udpchat;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    private DatagramSocket socket;
    private String username;

    @FXML
    private Button ChooseFileButton;

    @FXML
    private Text FileSize;

    @FXML
    private Text NameFile;

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

    @FXML
    private Button uploadButton;

    @FXML
    private ChoiceBox<String> fileDownloadChoiceBox;

    @FXML
    private Button downloadButton;


    private ArrayList<ServerFile> sharingFiles = new ArrayList<>();
    private String sourceFile;
    private int port;
    private  DataOutputStream dataOutputStream = null;
    private  DataInputStream dataInputStream = null;

    private void TCPServer()  {
        try{
            ServerSocket serverSocket = new ServerSocket(port);
            while (true){
                Socket clientSocket = serverSocket.accept();
                ServerProcess serverProcess = new ServerProcess(clientSocket, username);
                serverProcess.start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveFromServer() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                String[] parts = received.split(" ", 2);
                if(parts[0].equals("message")){
                    textArea.appendText(">>> " + parts[1] + "\n");
                } else if(parts[0].equals("file")){
                    String[] fileParts = parts[1].split(" ", 4);
                    String IP = fileParts[0];
                    String Port = fileParts[1];
                    String fileSize = fileParts[2];
                    String fileName = fileParts[3];
                    int port = Integer.parseInt(Port);
                    ServerFile sharingFile = new ServerFile(fileName, fileSize, IP, port);
                    sharingFiles.add(sharingFile);
                    fileDownloadChoiceBox.getItems().add(fileName);
                }

            }
        } catch (Exception ignored) {
        }
    }

    private void sendMessage(String message) {
        try {
            InetAddress address = InetAddress.getByName("localhost");
            int port = 1200;
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
            socket.send(packet);
        } catch (Exception ignored) {
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
    void handleSendButton() {
        String recipient = recipientText.getText();
        String message = messageText.getText();
        if(recipient != null && !recipient.isEmpty() && message != null && !message.isEmpty()) {
            sendMessage(recipient + " " +  username + " message " + message);
            textArea.appendText(">>> " + username + " to " + recipient + ": " + message + "\n");
            recipientText.clear();
            messageText.clear();
        }
        else{
            textArea.appendText(">>> Please enter a valid recipient and message\n");
        }
    }

    @FXML
    void handleEnterUsername() {
        username = usernameField.getText();
        File directory = new File("./user");
        if (! directory.exists()){
            directory.mkdir();
        }
        File directory1 = new File("./user/" + username);
        if (! directory1.exists()){
            directory1.mkdir();
        }
        File directory2 = new File("./user/" + username + "/download");
        if (! directory2.exists()){
            directory2.mkdir();
        }
        File directory3 = new File("./user/" + username + "/upload");
        if (! directory3.exists()){
            directory3.mkdir();
        }
        if(username != null && !username.isEmpty()) {
            textArea.appendText(">>> Welcome, " + username + "!\n");
            enterUsernameButton.setDisable(true);
            sendButton.setDisable(false);
            messageText.setDisable(false);
            recipientText.setDisable(false);
            usernameField.setDisable(true);
            try {
                sendMessage(username);
            }catch (Exception ignored) {
            }
        }
        else{
            textArea.appendText(">>> Please enter a valid username\n");
        }
    }

    @FXML
    void handleChooseFileButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
//        fileChooser.showOpenDialog(ChooseFileButton.getScene().getWindow());

        java.io.File file = fileChooser.showOpenDialog(ChooseFileButton.getScene().getWindow());
        if (file != null) {
            NameFile.setText(file.getName());
            FileSize.setText(file.length() + " bytes");
            //Print source file
            sourceFile = file.getAbsolutePath();
        }
    }

    void sendMessageTCP(String message, String IP, int port) {
        try {
            Socket socket = new Socket(IP, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            writer.flush();
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            receiveFile("./user/" + username + "/download/" + message);
            socket.close();
            dataInputStream.close();
            dataOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName)
            throws Exception
    {
        int bytes = 0;
        FileOutputStream fileOutputStream
                = new FileOutputStream(fileName);

        long size
                = dataInputStream.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = dataInputStream.read(
                buffer, 0,
                (int)Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        // Here we received file
        System.out.println("File is Received");
        fileOutputStream.close();
    }

    @FXML
    void handleDownloadButton() {
        String fileName = fileDownloadChoiceBox.getValue();
        String recipient = recipientText.getText();
        recipientText.clear();
        if(fileName != null && !fileName.isEmpty()) {
            for(ServerFile file : sharingFiles){
                if(file.getFileName().equals(fileName)){
//                    sendMessage(recipient + " " + username + " download " + file.getFileIP() + " " + file.getFilePort() + " " + fileName);
                    sendMessageTCP(fileName, file.getFileIP(), file.getFilePort());
                    //receiveMessageTCP(file.getFileIP(), file.getFilePort());

                    textArea.appendText(">>> " + username + " want to download file " + fileName + "\n");
                    break;
                }
            }
        }
        else{
            textArea.appendText(">>> Please select a file to download\n");
        }
    }

    @FXML
    void handleUploadButton() {
        String fileName = NameFile.getText();
        String recipient = recipientText.getText();
        String[] temp = FileSize.getText().split(" ");
        String fileSize = temp[0];
        if(recipient != null && !recipient.isEmpty() && fileName != null && !fileName.isEmpty()) {
            sendMessage(recipient + " " +  username + " file " + fileSize + " " + fileName );
            textArea.appendText(">>> " + username + " want to share file " + recipient + ": " + fileName + "\n");
            recipientText.clear();
            NameFile.setText("");
            FileSize.setText("");
            try{
                File source = new File(sourceFile);
                File dest = new File("./user/" + username + "/upload/" + fileName);
                Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            textArea.appendText(">>> Please enter a valid recipient and file\n");
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textArea.setEditable(false);
        sendButton.setDisable(true);
        messageText.setDisable(true);
        recipientText.setDisable(true);

        port = generateRandomPort();
        while (isPortInUse(port)) {
            port = generateRandomPort();
        }

        try{
            socket = new DatagramSocket(port);
            textArea.appendText(">>> Client started on port " + port + "\n");
            new Thread(this::receiveFromServer).start();
        } catch (Exception ignored) {
        }


        //TCP
        try {
            new Thread(this::TCPServer).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
