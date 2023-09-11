package org.example;

import lzu.utils.Component;
import logger.*;
import logger.Logger;
import lzu.utils.MessageQueue;

public class Main {

    @Inject
    static Logger logger;

    @Inject
    MessageQueue messageQueue;

    @Component(Component.Lifecycle.START)
    public void start() {
        String message = "hello from component a!";
        logger.sendLog("ComponentA: started.");
        messageQueue.sendMessage(message);
        logger.sendLog("Component A sent Message: " + message);
    }

    @Component(Component.Lifecycle.STOP)
    public void stop() {
        logger.sendLog("ComponentA: stopped.");
    }

    public static void main(String[] args) {
    }

}
