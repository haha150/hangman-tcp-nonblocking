package org.inlm2.server.net;

import org.inlm2.common.MessageHandler;
import org.inlm2.common.MessageType;
import org.inlm2.server.model.Game;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;

public class ClientHandler implements Runnable {

    private SocketChannel channel;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    private Server server;
    private int score;
    private Game game;
    private String hasMessageToSend;
    private final MessageHandler messageHandler = new MessageHandler();

    public ClientHandler(Server server, SocketChannel channel) {
        this.channel = channel;
        this.server = server;
        score = 0;
        game = null;
        hasMessageToSend = MessageType.NONE.toString() + "," + "Start a new game to begin!: Score: " + score;
    }

    @Override
    public void run() {
        try {
            while(messageHandler.hasNext()) {
                String[] arr = messageHandler.getFirstMessage().split(",");
                System.out.println(arr[0] + " ** " + arr[1]);
                if(arr[0].equalsIgnoreCase(MessageType.START_GAME.toString())) {
                    handleStartGame();
                    server.notifyServer();
                } else if (arr[0].equalsIgnoreCase(MessageType.GUESS.toString())) {
                    handleGuess(arr[1]);
                    server.notifyServer();
                } else if(arr[0].equalsIgnoreCase(MessageType.DISCONNECT.toString())) {
                    cleanUp();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void receiveMessage() throws IOException {
        buffer.clear();
        int bytes = channel.read(buffer);
        if(bytes != -1) {
            buffer.flip();
            byte[] byteArr = new byte[buffer.remaining()];
            buffer.get(byteArr);
            String message = new String(byteArr);
            System.out.println(message);
            messageHandler.addMessage(message);
            ForkJoinPool.commonPool().execute(this);
        } else {
            throw new IOException("Closed connection");
        }
    }

    public void sendMessage() throws IOException {
        String send = hasMessageToSend.length() + ",," + hasMessageToSend;
        ByteBuffer msg = ByteBuffer.wrap(send.getBytes());
        channel.write(msg);
        if(msg.hasRemaining()) {
            throw new IOException("Could not send message");
        }
        hasMessageToSend = null;
    }

    private void handleGuess(String m) {
        if(game.isGuessValid(m)) {
            game.guess(m.toLowerCase());
            if(game.hasWon()) {
                score++;
                hasMessageToSend = MessageType.GAME_OVER.toString() + "," + game.toString() + ": Score: " + score;
                game = null;
            } else if (game.getTries() == 0) {
                score--;
                hasMessageToSend = MessageType.GAME_OVER.toString() + "," + game.toString() + ": Score: " + score;
                game = null;
            } else {
                hasMessageToSend = MessageType.NONE.toString() + "," + game.toString()+ ": Score: " + score;
            }
        } else {
            hasMessageToSend = MessageType.NONE.toString() + "," + "Invalid guess: try again. guess one letter or the entire word";
        }
    }

    public String getHasMessageToSend() {
        return hasMessageToSend;
    }

    private void handleStartGame() {
        game = new Game(server.getRandomWord());
        hasMessageToSend = MessageType.NONE.toString() + "," + game.toString() + ", Score: " + score;
    }

    public void cleanUp() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }
}

