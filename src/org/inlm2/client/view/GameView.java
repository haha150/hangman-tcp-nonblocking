package org.inlm2.client.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.inlm2.client.controller.Controller;

public class GameView extends BorderPane {
    private final Stage primaryStage;
    private static TextArea textArea;
    private TextField textField;
    private Dialog dialog;
    private TextField ipField;
    private TextField portField;
    private MenuItem connect;
    private MenuItem disconnect;
    private MenuItem newGame;

    public GameView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initView();
    }

    public TextArea getTextArea() {
        return textArea;
    }

    private void initView() {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Server");
        connect = new MenuItem("Connect");
        disconnect = new MenuItem("Disconnect");
        menu.getItems().addAll(connect,disconnect);
        menuBar.getMenus().add(menu);

        Menu menuGame = new Menu("Game");
        newGame = new MenuItem("New game");
        menuGame.getItems().add(newGame);
        menuBar.getMenus().add(menuGame);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textField = new TextField();
        textField.setPromptText("Enter message");

        this.setTop(menuBar);
        this.setCenter(textArea);
        this.setBottom(textField);

        initConnectDialogView();
    }

    private void initConnectDialogView() {
        VBox container = new VBox();

        Label ip = new Label();
        ip.setText("IP:");
        ipField = new TextField();
        ipField.setPromptText("IP address");
        ipField.setText("localhost");   // tmp
        Label port = new Label();
        port.setText("Port:");
        portField = new TextField();
        portField.setText("12345");  // tmp
        portField.setPromptText("Port number");

        container.getChildren().addAll(ip,ipField,port,portField);

        ButtonType connectButton = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);

        dialog = new Dialog();
        dialog.setTitle("Connect");
        dialog.setResizable(false);
        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().getButtonTypes().addAll(connectButton);
    }

    public void addEventHandlers(Controller controller) {
        EventHandler<WindowEvent> closeHandler = event -> controller.handleClose(event, primaryStage);
        primaryStage.setOnCloseRequest(closeHandler);

        EventHandler<KeyEvent> enterMessageHandler = event -> controller.enterMessageHandler(event, textField);
        textField.setOnKeyPressed(enterMessageHandler);

        EventHandler<ActionEvent> connectHandler = event -> controller.connectHandler(dialog, ipField, portField);
        connect.setOnAction(connectHandler);

        EventHandler<ActionEvent> disconnectHandler = event -> controller.disconnectHandler();
        disconnect.setOnAction(disconnectHandler);

        EventHandler<ActionEvent> newGameHandler = event -> controller.newGameHandler();
        newGame.setOnAction(newGameHandler);
    }
}
