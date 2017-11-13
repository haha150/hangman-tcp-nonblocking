package org.inlm2.client.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.inlm2.client.net.Client;
import org.inlm2.client.net.OutputHandler;
import org.inlm2.client.view.GameView;

import java.io.IOException;

public class Controller {
    private Client client;
    private boolean connected = false;
    private boolean isGameOngoing = false;
    private static GameView view;
    private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

    public Controller(GameView view) {
        this.view = view;
    }

    public synchronized void appendText(String text) {
        if(isGameOngoing) {
            Platform.runLater(() -> view.getTextArea().appendText(text + "\n"));
        }
    }

    public synchronized void appendNewConnection(String text) {
        Platform.runLater(() -> view.getTextArea().appendText(text + "\n"));
    }

    public void connect(String ip, int port, OutputHandler outputHandler) {
        view.getTextArea().clear();
        disconnect();
        client = new Client(ip, port, outputHandler);
        try {
            client.connect();
            connected = true;
            outputHandler.handleNewConnection("Connected to " + ip + ":" + port);
        } catch (IOException ie) {
            appendText("Failed to establish connection.");
            try {
                client.disconnect();
                connected = false;
            } catch (Exception e) {
                System.out.println("Cleanup failed.");
            }
        }
    }

    public void disconnect() {
        if(connected) {
            try {
                client.disconnect();
                connected = false;
                isGameOngoing = false;
                appendText("Disconnected.");
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Failed to disconnect."));
            }
        }
    }

    public void sendGuessMessage(String guess) {
        try {
            if(isGameOngoing) {
                client.sendGuessMessage(guess);
            } else {
                Platform.runLater(() -> showAlert("Start a new game."));
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void sendNewGameMessage() {
        try {
            if(!isGameOngoing) {
                client.sendNewGameMessage();
                isGameOngoing = true;
            } else {
                Platform.runLater(() -> showAlert("Game already ongoing."));
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void showAlert(String message) {
        alert.setHeaderText("");
        alert.setTitle("Alert!");
        alert.setContentText(message);
        alert.show();
    }

    public void handleClose(WindowEvent event, Stage primaryStage) {
        event.consume();
        disconnect();
        primaryStage.close();
    }

    public void enterMessageHandler(KeyEvent event, TextField textField) {
        if(event.getCode().equals(KeyCode.ENTER)) {
            if(!connected) {
                showAlert("Connect first");
            } else {
                if(!textField.getText().isEmpty()) {
                    appendText("Guess: "+textField.getText());
                    sendGuessMessage(textField.getText());
                    textField.clear();
                }
            }
        }
    }

    public void connectHandler(Dialog dialog, TextField ipField, TextField portField) {
        dialog.showAndWait();
        if(!ipField.getText().isEmpty() && !portField.getText().isEmpty()) {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            if(port > 1024) {
                connect(ip, port, new ViewOutput());
                ipField.clear();
                portField.clear();
            } else {
                showAlert("Incorrect port");
            }
        }
    }


    public void newGameHandler() {
        sendNewGameMessage();
    }

    public void disconnectHandler() {
        disconnect();
    }

    private class ViewOutput implements OutputHandler {

        @Override
        public void handleNewConnection(String message) {
            appendNewConnection(message);
        }

        @Override
        public void handleMessage(String message) {
            appendText(message);
        }

        @Override
        public void handleGameOver() {
            appendText("Game over, start a new game to play again.");
            isGameOngoing = false;
        }
    }

}
