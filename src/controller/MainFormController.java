package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFormController {

    public TextArea txtCommand;
    public TextArea txtOutput;
    public Button btnExecute;
    public Label lblSchema;
    private Process mysql;

    public void initialize() {
        txtOutput.setWrapText(true);
    }

    public void initData(String host, String port, String username, String password) {
            ProcessBuilder mysqlBuilder = new ProcessBuilder("mysql",
                    "-h", host,
                    "-u", username,
                    "--port", port,
                    "-p",
                    "-n",
                    "-L",
                    "-f",
                    "-v",
                    "-v",
                    "-v");

        try {
            mysqlBuilder.redirectErrorStream(true);
            this.mysql = mysqlBuilder.start();

            processInputStream(mysql.getInputStream());

            this.mysql.getOutputStream().write((password + "\n").getBytes());
            this.mysql.getOutputStream().flush();

            txtCommand.getScene().getWindow().setOnCloseRequest(event -> {
                if (this.mysql.isAlive()) {
                    this.mysql.destroy();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to establish the connection");
            if (mysql.isAlive()) {
                mysql.destroyForcibly();
            }
            Platform.exit();
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

           txtOutput.clear();

           if (statement.equalsIgnoreCase("exit;")) {
               Platform.exit();
               return;
           }
           this.mysql.getOutputStream().write((statement+"\n").getBytes());
           this.mysql.getOutputStream().flush();
           txtCommand.selectAll();

            Pattern pattern = Pattern.compile(".*[;]?((?i)(use)) (?<db>[A-Za-z0-9-_]+);.*");
            Matcher matcher = pattern.matcher(statement);
            if (matcher.matches()){
                lblSchema.setText("SCHEMA: " + matcher.group("db"));
                txtOutput.setText("Database changed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processInputStream(InputStream inputStream) {
        Thread thread = new Thread(()->{
            try {
            while (true) {
                byte[] buffer = new byte[1024];
                int read = inputStream.read(buffer);

                if (read == -1) {
                    break;
                }

                String output = new String(buffer,0,read);
                Platform.runLater(()->{
                    txtOutput.appendText(output);

                    if (txtOutput.getText().contentEquals("Enter password: ")) {
                        txtOutput.clear();
                        txtOutput.setText("Welcome to MySQL shell\n" +
                                "======================================\n\n" +
                                "Please enter your command above to proceed. \nThank you!" +
                                "Copyright © 2022 BGT Nuwangi. All rights reserved\n");
                    }
                });
            }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
