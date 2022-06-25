package com.filekeeper.client;

import cloud.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    public TextArea hostTextArea;
    @FXML
    public TextArea usernameTextArea;
    @FXML
    VBox clientPanel, serverPanel;
    @FXML
    TextArea commandsTextArea;
    @FXML
    public HBox authPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public VBox workPanel;

    private Socket socket;
    private static final int PORT = 5000;
    private static final String ADDRESS = "localhost";

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    private boolean authenticated;
    private String login;
    private Stage stage;
    private Stage regStage;
    private RegistrationController regController;

    PanelController clientPl;
    PanelServerController serverPl;

    private String currentDir = System.getProperty("user.home") + "/Desktop";
    private String currentServerDir = "./UserFiles/";

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        workPanel.setVisible(authenticated);
        workPanel.setManaged(authenticated);

        hostTextArea.setText(ADDRESS);
        usernameTextArea.setText(login);

        if (!authenticated) {
            login = "";
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clientPl = (PanelController)clientPanel.getProperties().get("ctrl");
        serverPl = (PanelServerController) serverPanel.getProperties().get("ctrl");
        commandsTextArea.setStyle("-fx-font-size: 15;");
        connect();
    }

    public void CopyBtnAction(ActionEvent actionEvent) {
        PanelController clientPl = (PanelController)clientPanel.getProperties().get("ctrl");
        PanelServerController serverPl = (PanelServerController) serverPanel.getProperties().get("ctrl");
        currentDir = clientPl.getCurrentPath();
        currentServerDir = serverPl.getCurrentPath();

        if (clientPl.getSelectedFileName() == null && serverPl.getSelectedFileName() == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, "You didn't choose any file", ButtonType.CLOSE);
                alert.showAndWait();
            });
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

        if (srcPc == serverPl) {
            try {
                Fileinfo fileGet = serverPl.getTableView().getSelectionModel().getSelectedItem();
                getFile(fileGet.getFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                Fileinfo fileSend = clientPl.getTableView().getSelectionModel().getSelectedItem();
                sendFile(fileSend.getFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void DeleteBtnAction(ActionEvent actionEvent) {
        PanelController clientPl = (PanelController)clientPanel.getProperties().get("ctrl");
        PanelServerController serverPl = (PanelServerController) serverPanel.getProperties().get("ctrl");
        currentDir = clientPl.getCurrentPath();
        currentServerDir = serverPl.getCurrentPath();

        if (clientPl.getSelectedFileName() == null && serverPl.getSelectedFileName() == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, "You didn't choose any file", ButtonType.CLOSE);
                alert.showAndWait();
            });
            return;
        }

        PanelController srcPc = null;
        if (clientPl.getSelectedFileName() != null) {
            srcPc = clientPl;
        } else {
            srcPc = serverPl;
        }

        Path path = Paths.get(srcPc.getCurrentPath(), srcPc.getSelectedFileName());
        File file = new File(String.valueOf(path));

        if (srcPc == clientPl) {
            if(file.delete()){
                clientPl.updateList(Path.of(currentDir));
                commandsTextArea.appendText(file + "was deleted from your computer\n");
            }else {
                commandsTextArea.appendText(file + "wasn't deleted from your computer\n");
            }
        } else {
            try {
                write(new DeleteFile(String.valueOf(path)));
                write(new UpdateListFiles(login));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            serverPl.setOs(this.os);

            new Thread(() -> {
                try {
                    while (true) {
                        CloudMessage message = read();

                        if (message instanceof Authentication authentication) {
                            if (((Authentication) message).getAuthStatus()) {
                                Platform.runLater(() -> {
                                    login = loginField.getText();
                                    currentServerDir += login;
                                    setAuthenticated(true);
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                                            ("Hello, " + login + "! You are logged into your account"), ButtonType.CLOSE);
                                    alert.showAndWait();

                                });
                                write(new UpdateListFiles(login));
                                break;
                            } else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.WARNING,
                                            "Failed to log in! Login or password entered incorrectly", ButtonType.CLOSE);
                                    alert.showAndWait();
                                });
                            }
                        }
                        if (message instanceof Registration reg) {
                            if (reg.getRegStatus()) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                            "Your account has been successfully created!", ButtonType.OK);
                                    alert.showAndWait();
                                });
                            } else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.WARNING,
                                            "The specified login already exists :_( Try another one", ButtonType.OK);
                                    alert.showAndWait();
                                });
                            }
                        }
                    }


                    while (true) {
                        CloudMessage message = read();
                        if (message instanceof ListFiles listFiles) {
                            Platform.runLater(() -> {
                                ArrayList<Fileinfo> fileinfo = new ArrayList<>(Fileinfo.getFileInfoList(listFiles.getFiles()));
                                currentServerDir = listFiles.getPath();
                                serverPl.updateList(currentServerDir, fileinfo);
                                serverPl.pathField.setText(currentServerDir);
                            });
                        } else if (message instanceof FileMessage fileMessage) {
                            Path current = Path.of(currentDir).resolve(fileMessage.getName());
                            Files.write(current, fileMessage.getData());
                            LocalDateTime localDateTime = LocalDateTime.now();

                            Platform.runLater(() -> {
                                clientPl.updateList(Path.of(currentDir));
                            });
                        } else if (message instanceof DeleteFile deleteFile) {
                            if (deleteFile.getStatus()) {
                                commandsTextArea.appendText((new File(deleteFile.getPath()).getName() + "was deleted from the server\n"));
                            } else {
                                commandsTextArea.appendText((new File(deleteFile.getPath()).getName() + "wasn't deleted from the server\n"));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<Fileinfo> getFiles(String dir) {
        ArrayList<Fileinfo> fileinfoArrayList = new ArrayList<>();
        for (String file : new File(dir).list()) {
            fileinfoArrayList.add(new Fileinfo(Path.of(file)));
        }
        return fileinfoArrayList;
    }


    public void sendFile(String fileToSend) throws IOException {
        commandsTextArea.appendText("File " + fileToSend + " was sent to server!\n");
        write(new FileMessage(Path.of(currentDir).resolve(fileToSend)));
    }


    private void getFile(String fileToGet) throws IOException {
        commandsTextArea.appendText("File " + fileToGet + " was downloaded from server!\n");
        write(new FileRequest(fileToGet));
    }

    public CloudMessage read() throws IOException, ClassNotFoundException {
        return (CloudMessage) is.readObject();
    }

    public void write(CloudMessage msg) throws IOException {
        os.writeObject(msg);
        os.flush();
    }

    private void createRegStage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration.fxml"));
            Parent root = fxmlLoader.load();

            regStage = new Stage();

            regStage.setTitle("Filekeeper registration");
            regStage.setScene(new Scene(root, 600, 500));

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        Authentication auth = new Authentication(loginField.getText().trim(), passwordField.getText().trim());
        passwordField.clear();

        try {
            write(auth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegStage();
        }

        regStage.show();
    }

    public void registration(String login, String password) {
        Registration reg = new Registration(login, password);

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            write(reg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

