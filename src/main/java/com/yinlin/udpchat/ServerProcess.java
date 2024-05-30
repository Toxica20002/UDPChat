package com.yinlin.udpchat;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ServerProcess extends Thread{
    private Socket socket;
    private String username;
    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;

    public ServerProcess(Socket socket, String username){
        this.socket = socket;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            while(true){
                String message = reader.readLine();
                if(message == null){
                    break;
                }
                System.out.println(">>> " + message);
                //Send the file to the client
                sendFile( "./user/" + username + "/upload/" + message);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String path)
            throws Exception
    {
        int bytes = 0;
        // Open the File where he located in your pc
        File file = new File(path);
        FileInputStream fileInputStream
                = new FileInputStream(file);

        // Here we send the File to Server
        dataOutputStream.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        // close the file here
        fileInputStream.close();
    }
}
