package com.filekeeper.client;

import cloud.CloudMessage;
import cloud.CreateFolder;
import cloud.PathRequest;
import cloud.UpdateListFiles;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelServerController extends PanelController implements Initializable, Serializable {
    @FXML
    TableView<Fileinfo> tableView;
    @FXML
    TextField pathField;
    @FXML
    Button btnUp;
    @FXML
    public Button acceptNewFolderNameClient;
    @FXML
    public TextField newFolderNameClient;
    @FXML
    public Button newFolderButtonClient;

    private ObjectEncoderOutputStream os;

    public void setOs(ObjectEncoderOutputStream os) {
        this.os = os;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<Fileinfo, String> fileTypeColumn = new TableColumn<>("Type");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<Fileinfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(200);

        TableColumn<Fileinfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(100);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        TableColumn<Fileinfo, Long> fileDateColumn = new TableColumn<>("Date");
        fileDateColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getLastModified().format(dateTimeFormatter)));
        fileDateColumn.setPrefWidth(180);

        tableView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);
        tableView.getSortOrder().add(fileTypeColumn);

        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<Fileinfo, Long>() {
                @Override
                protected void updateItem(Long aLong, boolean b) {
                    super.updateItem(aLong, b);
                    if (aLong == null || b) {
                        setText("");
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", aLong);
                        if (aLong == -1L){
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Path path = Paths.get(getCurrentPath()).resolve(tableView.getSelectionModel().getSelectedItem().getFilename());
                    if (tableView.getSelectionModel().getSelectedItem().getFilename().indexOf(".") == -1){
                        try {
                            write(new PathRequest(path));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public TableView<Fileinfo> getTableView() {
        return tableView;
    }

    private List<String> getFiles(String dir) {
        String [] list = new File(dir).list();
        return Arrays.asList(list);
    }

    public void updateList(String path, ArrayList<Fileinfo> fileInfo) {
        pathField.setText(path);
        tableView.getItems().clear();
        tableView.getItems().addAll(fileInfo);
        tableView.sort();
    }

    public void btnUpPathActionServer(ActionEvent actionEvent) {
        String currentPath = getCurrentPath();
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (!(upperPath.equals(Path.of("./UserFiles")))) {
            try {
                write(new PathRequest(upperPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSelectedFileName() {
        if (!tableView.isFocused()) {
            return null;
        }
        return this.tableView.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }

    public void write(CloudMessage msg) throws IOException {
        os.writeObject(msg);
        os.flush();
    }

    public void acceptNewFolderNameClient(ActionEvent actionEvent) {
        String newFolderFileName = "";

        if (newFolderNameClient.getText().length() == 0) {
            newFolderFileName = "New Folder";
        }
        if (newFolderNameClient.getText().indexOf(".") >= 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, "There should be no dot in the folder name", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        newFolderFileName = newFolderNameClient.getText();
        try {
            CreateFolder folder = new CreateFolder(getCurrentPath(), newFolderFileName);
            write(folder);
        } catch (IOException e) {
            System.out.println("Exception");
            e.printStackTrace();
        }

        newFolderButtonClient.setVisible(true);
        newFolderButtonClient.setManaged(true);
        newFolderNameClient.setVisible(false);
        newFolderNameClient.setManaged(false);
        acceptNewFolderNameClient.setVisible(false);
        acceptNewFolderNameClient.setManaged(false);
    }

    public void createNewFolderClient(ActionEvent actionEvent) {
        newFolderButtonClient.setVisible(false);
        newFolderButtonClient.setManaged(false);
        newFolderNameClient.setVisible(true);
        newFolderNameClient.setManaged(true);
        acceptNewFolderNameClient.setVisible(true);
        acceptNewFolderNameClient.setManaged(true);
    }

}
