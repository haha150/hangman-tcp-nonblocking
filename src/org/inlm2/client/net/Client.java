package org.inlm2.client.net;

import org.inlm2.common.Message;
import org.inlm2.common.MessageType;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private static final int HOUR = 1000 * 60 * 60;
    private InetAddress serverAddress;
    private Socket socket;
    private ObjectOutputStream sout;
    private static String IP;
    private static int SERVER_PORT;
    private Thread receiver;
    private OutputHandler outputHandler;

    public Client(String ip, int port, OutputHandler outputHandler) {
        this.IP = ip;
        this.SERVER_PORT = port;
        this.outputHandler = outputHandler;
    }

    public void sendMessage(MessageType messageType, String message) throws IOException {
        Message m = new Message(messageType, message);
        sout.writeObject(m);
        sout.flush();
        sout.reset();
    }

    public void sendGuessMessage(String guess) throws IOException {
        sendMessage(MessageType.GUESS, guess);
    }

    public void sendNewGameMessage() throws IOException {
        sendMessage(MessageType.START_GAME, "");
    }

    public void sendDisconnectMessage() throws IOException {
        sendMessage(MessageType.DISCONNECT, "");
    }

    public void connect() throws IOException {
        serverAddress = InetAddress.getByName(IP);
        socket = new Socket(serverAddress, SERVER_PORT);
        sout = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream sin = new ObjectInputStream(socket.getInputStream());
        socket.setSoTimeout(HOUR);
        socket.setKeepAlive(true);
        receiver = new Thread(new Receiver(sin, outputHandler));
        receiver.start();
    }

    public void disconnect() throws IOException {
        sendDisconnectMessage();
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    private class Receiver implements Runnable {

        private ObjectInputStream sin;
        private OutputHandler outputHandler;

        public Receiver(ObjectInputStream sin, OutputHandler outputHandler) {
            this.sin = sin;
            this.outputHandler = outputHandler;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Message m = (Message)sin.readObject();
                    outputHandler.handleMessage(m.getMessage());
                    if(m.getMessageType() == MessageType.GAME_OVER) {
                        outputHandler.handleGameOver();
                    }
                }
            } catch (IOException | ClassNotFoundException e2) {

            }
        }
    }
}