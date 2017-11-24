package org.inlm2.client.net;

import org.inlm2.common.MessageHandler;
import org.inlm2.common.MessageType;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;

public class Client implements Runnable {

    private InetSocketAddress serverAddress;
    private static String IP;
    private static int SERVER_PORT;
    private OutputHandler outputHandler;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    private boolean connected;
    private SocketChannel socketChannel;
    private Selector selector;
    private boolean first, send;
    private String message;
    private final MessageHandler messageHandler;

    public Client(String ip, int port, OutputHandler outputHandler) {
        this.IP = ip;
        this.SERVER_PORT = port;
        this.outputHandler = outputHandler;
        connected = false;
        first = true;
        send = false;
        message = null;
        messageHandler = new MessageHandler();
    }

    public void sendMessage(MessageType messageType, String msg) throws IOException {
        message = messageType.toString() + "," + msg;
        message = message.length() + ",," + message;
        send = true;
        selector.wakeup();
    }

    public void sendGuessMessage(String guess) throws IOException {
        sendMessage(MessageType.GUESS, guess);
    }

    public void sendNewGameMessage() throws IOException {
        sendMessage(MessageType.START_GAME, "start");
    }

    public void sendDisconnectMessage() throws IOException {
        sendMessage(MessageType.DISCONNECT, "disconnect");
    }

    public void connect() throws IOException {
        serverAddress = new InetSocketAddress(IP, SERVER_PORT);
        connected = true;
        new Thread(this).start();
    }

    public void disconnect() throws IOException {
        sendDisconnectMessage();
        connected = false;
    }

    private void doDisconnect() throws IOException {
        connected = false;
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
        ForkJoinPool.commonPool().execute(() -> {
            outputHandler.handleNewConnection("Disconnected");
        });
    }

    @Override
    public void run() {
        try {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while(connected) {
            if(send) {
                socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                send = false;
            }
            selector.select();
            for (SelectionKey key : selector.selectedKeys()) {
                selector.selectedKeys().remove(key);
                if (key.isValid()) {
                    if (key.isConnectable()) {
                        socketChannel.finishConnect();
                        key.interestOps(SelectionKey.OP_READ);
                        outputHandler.handleMessage("Connected to " + IP);
                    } else if (key.isReadable()) {
                        handleReceivedMessage();
                    } else if (key.isWritable()) {
                        handleSendMessage(key);
                    }
                }
            }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                doDisconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSendMessage(SelectionKey key) throws IOException {
        ByteBuffer msg = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(msg);
        if(msg.hasRemaining()) {
            return;
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void handleReceivedMessage() throws IOException {
        buffer.clear();
        int bytes = socketChannel.read(buffer);
        if(bytes == -1) {
            throw new IOException("Failed to receive entire message");
        }
        buffer.flip();
        byte[] byteArr = new byte[buffer.remaining()];
        buffer.get(byteArr);
        String rcv = new String(byteArr);
        messageHandler.addMessage(rcv);
        while (messageHandler.hasNext()) {
            String msg = messageHandler.getFirstMessage();
            String[] arr = msg.split(",");
            ForkJoinPool.commonPool().execute(() -> {
                if (arr[0].equalsIgnoreCase(MessageType.GAME_OVER.toString())) {
                    outputHandler.handleGameOver(arr[1]);
                } else if(arr[0].equalsIgnoreCase(MessageType.NONE.toString())) {
                    if (first) {
                        outputHandler.handleNewConnection(arr[1]);
                        first = false;
                    } else {
                        outputHandler.handleMessage(arr[1]);
                    }
                }
            });
        }

    }
}