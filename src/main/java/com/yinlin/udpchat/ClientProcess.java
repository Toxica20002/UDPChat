package com.yinlin.udpchat;

import java.io.*;
import java.net.Socket;

public class ClientProcess extends Thread {
    private Socket socket;
    private String username;

    public ClientProcess(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            byte[] bytes = new byte[16*1024];
            File file = new File("./user/" + username + "/download/");
            if (!file.exists()) {
                file.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(file + "/receivedFile");
            int count;
            while ((count = is.read(bytes)) > 0) {
                fos.write(bytes, 0, count);
            }
            fos.close();
            is.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
