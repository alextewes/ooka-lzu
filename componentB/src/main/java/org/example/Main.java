package org.example;

import lzu.utils.Component;
import logger.Inject;
import logger.Logger;
import lzu.utils.MessageQueue;

public class Main {
    @Inject
    static Logger logger;

    @Inject
    MessageQueue messageQueue;

    @Component(Component.Lifecycle.START)
    public void start() {
        logger.sendLog("ComponentB: started.");

        new Thread(() -> {
            while (true) {
                String receivedMessage = messageQueue.receiveMessage();
                if (receivedMessage != null) {
                    String processedMessage = receivedMessage.toUpperCase();
                    logger.sendLog("ComponentB received and processed the message: " + processedMessage);
                }
            }
        }).start();
    }
    @Component(Component.Lifecycle.STOP)
    public void stop() {
        logger.sendLog("ComponentB: stopped.");
    }
    public static void main(String[] args) {

    }

}
