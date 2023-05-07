package org.example;

import logger.Inject;
import logger.Logger;
import logger.LoggerFactory;

public class Bar {

    public void sayHello() {
        Logger logger = LoggerFactory.createLogger();
        logger.sendLog("ComponentB: Started org.example.Bar!");
    }
}
