package com.filekeeper.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    VBox clientPanel, serverPanel;
    @FXML
    TextArea commandsTextArea;

    private Socket socket;
    private static final int PORT = 5000;
    private static final String ADDRESS = "localhost";

    private DataInputStream in;
    private DataOutputStream out;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private InputStream is;
    private OutputStream os;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private String serverDirectory;
    private byte[] buf;

//    PanelController clientPl;
//    PanelController serverPl;


    private String HOMEDIR = System.getProperty("user.home") + "/Desktop";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        PanelController clientPl = (PanelController)clientPanel.getProperties().get("ctrl");
//        PanelController serverPl = (PanelController)serverPanel.getProperties().get("ctrl");
        connect();
    }

    public void CopyBtnAction(ActionEvent actionEvent) {
        PanelController clientPl = (PanelController)clientPanel.getProperties().get("ctrl");
        PanelController serverPl = (PanelController)serverPanel.getProperties().get("ctrl");

        if (clientPl.getSelectedFileName() == null && serverPl.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You didn't choose any file", ButtonType.CLOSE);
            alert.showAndWait();
            return;
        }

        PanelController srcPc = null, dstPc = null;
        if (clientPl.getSelectedFileName() != null) {
            srcPc = clientPl;
            dstPc = serverPl;
        } else {
            srcPc = serverPl;
            dstPc = clientPl;
        }

        Path srcPath = Paths.get(srcPc.getCurrentPath(), srcPc.getSelectedFileName());
        Path dstPath = Paths.get(dstPc.getCurrentPath()).resolve(srcPath.getFileName().toString());

        Fileinfo fi = new Fileinfo(srcPath);

        if (srcPc == serverPl) {
            try {
                String message = "RECEIVEFILE@" + srcPath.toString();
                System.out.println(message);
                sendMsg(message);
                getFile(dstPath.getParent() + "\\" + srcPath.getFileName(), fi.getSize());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }
        else {
            try {
                String message = "SENDFILE@" + serverPl.pathField.getText() + "\\" + srcPc.getSelectedFileName() + "%%" + fi.getSize();
                System.out.println(message);
                sendMsg(message);
                sendFile(srcPath.toString());
            } catch (IOException e) {
            }
        }
    }

    public void DeleteBtnAction(ActionEvent actionEvent) {
        PanelController clientPl = (PanelController)clientPanel.getProperties().get("ctrl");
        PanelController serverPl = (PanelController)serverPanel.getProperties().get("ctrl");

        if (clientPl.getSelectedFileName() == null && serverPl.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You didn't choose any file", ButtonType.CLOSE);
            alert.showAndWait();
            return;
        }

        PanelController srcPc = null;
        if (clientPl.getSelectedFileName() != null) {
            srcPc = clientPl;
        } else {
            srcPc = serverPl;
        }

        Path srcPath = Paths.get(srcPc.getCurrentPath(), srcPc.getSelectedFileName());

        try {
            if (srcPc == serverPl) {
                sendMsg("@DELETE%%%" + srcPath);
            }
            Files.delete(srcPath);
            srcPc.updateList(Paths.get(srcPc.getCurrentPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            ArrayList<Fileinfo> fileinfo = new ArrayList<>();
            PanelServerController serverPl = (PanelServerController) serverPanel.getProperties().get("ctrl");

            new Thread(() -> {
                try {
                    String fromServer;
                    while (true) {
                        fromServer = in.readUTF();
                        System.out.println("I got msg!");
                        if (fromServer.startsWith("FILEINFO@")) {
                            String[] firstSplit = fromServer.split("@");
                            String[] secondSplit = firstSplit[1].split("&");
                            for (int i = 0; i < secondSplit.length; i++) {
                                String objLine = secondSplit[i];
                                if (objLine.split("%%").length == 4) {
                                    fileinfo.add(new Fileinfo(objLine.split("%%")[0], Fileinfo.getType(objLine.split("%%")[1]), Integer.valueOf(objLine.split("%%")[2]), LocalDateTime.parse( objLine.split("%%")[3])));
                                }
                            }
                            serverDirectory = secondSplit[0];
                            serverPl.updateList(serverDirectory, fileinfo);
                        }
                        if (fromServer.startsWith("CLIENTCOMMANDLINE@")) {
                            commandsTextArea.appendText(fromServer.split("@")[1] + "\n");
                        }
                        System.out.println("Server sent: " + fromServer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendMsg(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFile(String fileReceive, long fileSize) throws IOException {
        byte [] mybytearray  = new byte [Math.toIntExact(fileSize)];
        int bytesRead;
        int current = 0;
        fos = new FileOutputStream(fileReceive);
        bos = new BufferedOutputStream(fos);
        bytesRead = is.read(mybytearray,0,mybytearray.length);
        current = bytesRead;

        do {
            bytesRead =
                    is.read(mybytearray, current, (mybytearray.length-current));
            if(bytesRead >= 0) current += bytesRead;
        } while(bytesRead > -1);

        bos.write(mybytearray, 0 , current);
        bos.flush();
        System.out.println("File " + fileReceive
                + " downloaded (" + current + " bytes read)");
    }


    private void sendFile(String fileToSend) throws IOException {
        // send file
        File myFile = new File (fileToSend);
        byte [] mybytearray  = new byte [(int)myFile.length()];
        fis = new FileInputStream(myFile);
        bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        System.out.println("Sending " + fileToSend + "(" + mybytearray.length + " bytes)");
        os.write(mybytearray,0,mybytearray.length);
        os.flush();
        System.out.println("Done.");
    }


}

