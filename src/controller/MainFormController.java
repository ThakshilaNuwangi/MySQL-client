package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStream;

public class MainFormController {

    public TextArea txtCommand;
    public TextArea txtOutput;
    public Button btnExecute;
    private Process mysql;

    public void initData(String host, String port, String username, String password) {
        try {
            mysql = new ProcessBuilder("mysql",
                    "-h", host,
                    "--port", port,
                    "-u", username,
                    "-p").start();

            mysql.getOutputStream().write(password.getBytes());
            mysql.getOutputStream().flush();

            txtCommand.getScene().getWindow().setOnCloseRequest(event -> {
                if (mysql.isAlive()) {
                    mysql.destroy();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to establish the connection").show();
            if (mysql.isAlive()) {
                mysql.destroyForcibly();
            }
        }
    }

    public void btnExecuteOnAction(ActionEvent actionEvent) {
        String statement = txtCommand.getText();
        if (!statement.endsWith(";")) {
            statement += ";";
        }
        try {
            System.out.println(mysql.isAlive());
            mysql.getOutputStream().write(statement.getBytes());
            mysql.getOutputStream().flush();

            InputStream is = mysql.getErrorStream();
            byte[] buffer = new byte[1024];
            System.out.println(is.read(buffer));
            txtOutput.setText(new String(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
