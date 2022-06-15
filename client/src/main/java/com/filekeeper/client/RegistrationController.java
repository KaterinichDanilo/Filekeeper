package com.filekeeper.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    HelloController controller;

    public void setController(HelloController controller) {
        this.controller = controller;
    }

    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        controller.registration(login, password);
    }

    public void showResult(boolean authStatus) {
        if (authStatus) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "You are logged in to your account!", ButtonType.CLOSE);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Such a login already exists! Try another", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }
}
