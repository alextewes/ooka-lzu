package lzu.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    private static final MessageQueue instance = new MessageQueue();
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public static MessageQueue getInstance() {
        return instance;
    }

    public void sendMessage(String message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String receiveMessage() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
