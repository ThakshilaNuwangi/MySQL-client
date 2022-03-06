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

    public void initialize () {
        txtOutput.setWrapText(true);
    }

    public void initData(String host, String port, String username, String password) {
        try {
             ProcessBuilder mysqlBuilder= new ProcessBuilder("mysql",
                    "-h", host,
                    "-u", username,
                    "--port", port,
                    "-n",
                    "-p",
                    "-v");
            this.mysql=mysqlBuilder.start();

            this.mysql.getOutputStream().write((password+"\n").getBytes());
            this.mysql.getOutputStream().flush();

            txtCommand.getScene().getWindow().setOnCloseRequest(event -> {
                if (this.mysql.isAlive()) {
                    this.mysql.destroy();
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
            /*System.out.println(mysql.isAlive());
            mysql.getOutputStream().write(statement.getBytes());
            mysql.getOutputStream().flush();

            InputStream is = mysql.getErrorStream();
            byte[] buffer = new byte[1024];
            System.out.println(is.read(buffer));
            txtOutput.setText(new String(buffer));*/

            this.mysql.getOutputStream().write((statement + "\n").getBytes());
            this.mysql.getOutputStream().flush();

            InputStream is = this.mysql.getInputStream();
            byte[] buffer = new byte[1024];
            System.out.println(is.read(buffer));
            txtOutput.setText(new String(buffer));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
