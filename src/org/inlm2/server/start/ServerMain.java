package org.inlm2.server.start;

import org.inlm2.server.net.Server;

public class ServerMain {
    public static void main(String[] args) {
        Server s = new Server();
        s.start();
    }
}
