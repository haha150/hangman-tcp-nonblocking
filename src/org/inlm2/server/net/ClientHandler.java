package org.inlm2.server.net;

import org.inlm2.common.Message;
import org.inlm2.common.MessageType;
import org.inlm2.server.model.Game;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {

    private static final int HOUR = 1000 * 60 * 60;
    private Socket socket;
    private ObjectOutputStream sout;
    private ObjectInputStream  sin;
    private Server server;
    private boolean running = true;
    private int score;
    private Game game;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            this.socket.setSoTimeout(HOUR);
        } catch (SocketException e) {
            System.out.println("Failed to set timeout.");
        }
        score = 0;
        game = null;
    }

    @Override
    public void run() {
        try {
            sin = new ObjectInputStream(socket.getInputStream());
            sout = new ObjectOutputStream(socket.getOutputStream());
            sendMessage(MessageType.NONE, "Start a new game to begin!, Score: " + score);
            while(running) {
                Message m = (Message) sin.readObject();
                switch (m.getMessageType()) {
                    case START_GAME:
                        handleStartGame();
                        break;
                    case GUESS:
                        handleGuess(m);
                        break;
                    case DISCONNECT:
                        running = false;
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                cleanUp();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private void sendMessage(MessageType messageType, String message) {
        try {
            Message m = new Message(messageType, message);
            sout.writeObject(m);
            sout.flush();
            sout.reset();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void handleGuess(Message m) {
        if(game.isGuessValid(m.getMessage())) {
            game.guess(m.getMessage().toLowerCase());
            if(game.hasWon()) {
                score++;
                sendMessage(MessageType.GAME_OVER, game.toString()+ ", Score: " + score);
                game = null;
            } else if (game.getTries() == 0) {
                score--;
                sendMessage(MessageType.GAME_OVER, game.toString()+ ", Score: " + score);
                game = null;
            } else {
                sendMessage(MessageType.NONE, game.toString()+ ", Score: " + score);
            }
        } else {
            sendMessage(MessageType.NONE, "Invalid guess, try again, guess one letter or the entire word");
        }
    }

    private void handleStartGame() {
        game = new Game(server.getRandomWord());
        sendMessage(MessageType.NONE, game.toString()+ ", Score: " + score);
    }

    private void cleanUp() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if(sin != null) {
            sin.close();
        }
        if(sout != null) {
            sout.close();
        }
    }
}

