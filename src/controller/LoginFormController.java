package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
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

    public void initialize() {
        Platform.runLater(() -> {
            txtUsername.requestFocus();
        });
    }

    public void btnConnectOnAction(ActionEvent actionEvent) {
        validate();
        try {

            //sometimes the following code gives errors cuz String formatting syntax can be different for each OS
            /*String command = String.format("mysql -h %s -u %s -p%s --port %s -e exit",
                    txtHost.getText(),
                    txtUsername.getText(),
                    txtPassword.getText(),
                    txtPort.getText());
            Process mysql = Runtime.getRuntime().exec(command);*/


            //Following 2 ways are better than the previous one
            /*String[] commands = {"mysql",
            "-h", txtHost.getText(),
                    "-u", txtUsername.getText(),
                    "--port", txtPort.getText(),
                    "-p"+txtPassword.getText(),
                    "-e", "exit"
            };
            Process mysql = Runtime.getRuntime().exec(commands);*/

            /*Process mysql = new ProcessBuilder("mysql",
                    "-h", txtHost.getText(),
                    "-u", txtUsername.getText(),
                    "--port", txtPort.getText(),
                    "-p" + txtPassword.getText(),
                    "-e", "exit").start();*/

            //Remove warning message by supplying password through an output stream
            Process mysql = new ProcessBuilder("mysql",
                    "-h", txtHost.getText(),
                    "-u", txtUsername.getText(),
                    "--port", txtPort.getText(),
                    "-p",
                    "-e", "exit").start();

            mysql.getOutputStream().write(txtPassword.getText().getBytes());
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
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.show();

                txtUsername.requestFocus();
                txtUsername.selectAll();
            } else {
                System.out.println("Success");
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainForm.fxml"));
                AnchorPane root = fxmlLoader.load();
                Scene MainScene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(MainScene);
                MainFormController controller = fxmlLoader.getController();
                controller.initData(txtHost.getText(),
                        txtPort.getText(),
                        txtUsername.getText(),
                        txtPassword.getText());
                stage.centerOnScreen();
                stage.setTitle("MySQL Client Shell");
                stage.show();
                ((Stage) (txtUsername.getScene().getWindow())).close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void btnCancelOnAction(ActionEvent actionEvent) {
        System.exit(0);
    }

    private void validate() {
        if (txtHost.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Host can't be empty").show();
            txtHost.requestFocus();
            txtHost.selectAll();
            return;
        } else if (!txtPort.getText().matches("\\d+")) {
            new Alert(Alert.AlertType.ERROR, "Invalid Port").show();
            txtPort.requestFocus();
            txtPort.selectAll();
            return;
        } else if (txtUsername.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Username can't be empty").show();
            txtUsername.requestFocus();
            txtUsername.selectAll();
        }
    }
}