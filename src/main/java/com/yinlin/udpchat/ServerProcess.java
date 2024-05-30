package com.yinlin.udpchat;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ServerProcess extends Thread{
    private Socket socket;
    private String username;

    public ServerProcess(Socket socket, String username){
        this.socket = socket;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while(true){
                String message = reader.readLine();
                if(message == null){
                    break;
                }
                //Send the file to the client
                File file = new File("./user/" + username + "/" + message);
                byte[] bytes = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(bytes);
                fis.close();

                OutputStream os = socket.getOutputStream();
                os.write(bytes, 0, bytes.length);
                os.flush();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
