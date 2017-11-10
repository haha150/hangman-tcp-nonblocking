package org.inlm2.client.net;

public interface OutputHandler {

    public void handleMessage(String message);

    public void handleGameOver();

    public void handleNewConnection(String message);
}
