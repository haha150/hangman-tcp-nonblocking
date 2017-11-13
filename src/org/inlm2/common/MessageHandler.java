package org.inlm2.common;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler {

    private final List<String> messages = new ArrayList<String>();
    private List<String> received;

    public MessageHandler() {
        received = new ArrayList<String>();
    }

    public synchronized void addMessage(String message) {
        received.add(message);
        while(extractMessage());
    }

    public synchronized String getFirstMessage() {
        String top = messages.get(0);
        messages.remove(0);
        return top;
    }

    public synchronized boolean hasNext() {
        return !messages.isEmpty();
    }

    private boolean extractMessage() {
        String recv = "";
        for(String s : received) {
            recv += s;
        }
        String[] arr = recv.split(",,");
        if(arr.length < 2) {
            return false;
        }
        int len = Integer.parseInt(arr[0]);
        if(arr[1].length() >= len) {
            String msg = arr[1].substring(0, len);
            messages.add(msg);
            received.clear();
            received.add(recv.substring(arr[0].length()+2+len, recv.length()));
            return true;
        }
        return false;
    }
}
