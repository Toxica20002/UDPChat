package com.yinlin.udpchat;

public class ServerFile {
    private String fileName;
    private String fileSize;
    private String fileIP;
    private int filePort;
    private String sourceFile;
    private String ID;

    public ServerFile(String fileName, String fileSize, String fileIP, int filePort) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileIP = fileIP;
        this.filePort = filePort;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileIP() {
        return fileIP;
    }

    public void setFileIP(String fileIP) {
        this.fileIP = fileIP;
    }

    public int getFilePort() {
        return filePort;
    }

    public void setFilePort(int filePort) {
        this.filePort = filePort;
    }

    @Override
    public String toString() {
        return "SharingFile{" +
                "fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", fileIP='" + fileIP + '\'' +
                ", filePort=" + filePort +
                '}';
    }
}
