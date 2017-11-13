package org.inlm2.server.net;

import org.inlm2.server.controller.Controller;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

public class Server {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private static final int PORT = 12345;
    private Controller controller;
    private volatile boolean send = false;

    public Server() {
        controller = new Controller();
    }

    public void notifyServer() {
        send = true;
        selector.wakeup();
    }

    private void makeClientWrite() {
        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel && key.isValid()) {
                Client c = (Client) key.attachment();
                if (c.handler.getHasMessageToSend() != null) {
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            }
        }
    }

    public void start() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            InetSocketAddress addr = new InetSocketAddress(PORT);
            serverSocketChannel.bind(addr);
            int op = SelectionKey.OP_ACCEPT;
            serverSocketChannel.register(selector, op);
            System.out.println("Server is listening...");
            for(;;) {
                if(send) {
                    makeClientWrite();
                    send = false;
                }
                selector.select();
                handleKeys(selector.selectedKeys());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKeys(Set<SelectionKey> keys) throws IOException {
        Iterator<SelectionKey> iterator = keys.iterator();
        while(iterator.hasNext()) {
            SelectionKey key = iterator.next();
            if(key.isValid()) {
                if(key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
            iterator.remove();
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.handler.sendMessage();
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            cleanUp(key);
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        ClientHandler handler = new ClientHandler(this, channel);
        channel.register(selector, SelectionKey.OP_WRITE, new Client(handler));
        channel.setOption(StandardSocketOptions.SO_LINGER, 5000);
    }

    private void handleRead(SelectionKey key) throws IOException {
        Client c = (Client) key.attachment();
        try {
            c.handler.receiveMessage();
        } catch (IOException e) {
            cleanUp(key);
        }
    }

    private void cleanUp(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        client.handler.cleanUp();
        key.cancel();
    }


    public synchronized String getRandomWord() {
        return controller.getRandomWord();
    }

    private class Client {

        private ClientHandler handler;

        private Client(ClientHandler handler) {
            this.handler = handler;
        }
    }

}

