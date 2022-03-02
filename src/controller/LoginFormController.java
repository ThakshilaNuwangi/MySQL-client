package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class LoginFormController {
    public TextField txtHost;
    public TextField txtUsername;
    public TextField txtPort;
    public PasswordField txtPassword;
    public Button btnCancel;
    public Button btnConnect;

    public LoginFormController() {
    }

    public void initialize() {
        Platform.runLater(() -> {
            this.txtUsername.requestFocus();
        });
    }

    public void btnConnectOnAction(ActionEvent actionEvent) {
        this.validate();

        try {
            Process mysql = (new ProcessBuilder(new String[]{"mysql", "-h", this.txtHost.getText(), "-u", this.txtUsername.getText(), "--port", this.txtPort.getText(), "-p", "-e", "exit"})).start();
            mysql.getOutputStream().write(this.txtPassword.getText().getBytes());
            mysql.getOutputStream().close();
            int exitCode = mysql.waitFor();
            if (exitCode != 0) {
                InputStream errorStream = mysql.getErrorStream();
                byte[] buffer = new byte[errorStream.available()];
                errorStream.read(buffer);
                errorStream.close();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Failure");
                alert.setHeaderText("Can't establish the connection");
                alert.setContentText(new String(buffer));
                alert.getDialogPane().setMinHeight(-1.0D / 0.0);
                alert.show();
                this.txtUsername.requestFocus();
                this.txtUsername.selectAll();
            } else {
                System.out.println("Success");
                FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/MainForm.fxml"));
                AnchorPane root = (AnchorPane)fxmlLoader.load();
                Scene MainScene = new Scene(root);
                Stage stage = (Stage)this.txtUsername.getScene().getWindow();
                stage.setScene(MainScene);
                MainFormController controller = (MainFormController)fxmlLoader.getController();
                controller.initData(this.txtHost.getText(), this.txtPort.getText(), this.txtUsername.getText(), this.txtPassword.getText());
                stage.centerOnScreen();
                stage.setTitle("MySQL Client Shell");
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }

    public void btnCancelOnAction(ActionEvent actionEvent) {
        System.exit(0);
    }

    private void validate() {
        if (this.txtHost.getText().trim().isEmpty()) {
            (new Alert(Alert.AlertType.ERROR, "Host can't be empty", new ButtonType[0])).show();
            this.txtHost.requestFocus();
            this.txtHost.selectAll();
        } else if (!this.txtPort.getText().matches("\\d+")) {
            (new Alert(Alert.AlertType.ERROR, "Invalid Port", new ButtonType[0])).show();
            this.txtPort.requestFocus();
            this.txtPort.selectAll();
        } else {
            if (this.txtUsername.getText().trim().isEmpty()) {
                (new Alert(Alert.AlertType.ERROR, "Username can't be empty", new ButtonType[0])).show();
                this.txtUsername.requestFocus();
                this.txtUsername.selectAll();
            }
        }
    }
}
