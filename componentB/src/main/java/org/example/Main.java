package org.example;

import lzu.utils.Component;
import lzu.utils.logger.ConsoleLogger;
import lzu.utils.logger.Inject;
import lzu.utils.MessageQueue;

public class Main {
    @Inject
    static ConsoleLogger logger;

    @Inject
    static MessageQueue messageQueue;

    @Component(Component.Lifecycle.START)
    public void start() {
        logger.sendLog("ComponentB: started.");

        new Thread(() -> {
            while (true) {
                String receivedMessage = messageQueue.receiveMessage();
                if (receivedMessage != null) {
                    String cleanMessage = receivedMessage.replaceAll("\\[|\\]", "");
                    String[] numberStrings = cleanMessage.split(", ");
                    int sum = 0;

                    for (String numStr : numberStrings) {
                        try {
                            sum += Integer.parseInt(numStr);
                        } catch (NumberFormatException e) {
                            logger.sendLog("Invalid number format: " + numStr);
                        }
                    }

                    logger.sendLog("ComponentB: Sum of received primes: " + sum);
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
