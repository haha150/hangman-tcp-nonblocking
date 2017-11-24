package org.inlm2.client.net;

public interface OutputHandler {

    public void handleMessage(String message);

    public void handleGameOver(String message);

    public void handleNewConnection(String message);
}
