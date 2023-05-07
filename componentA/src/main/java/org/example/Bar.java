package org.example;

import logger.Logger;
import logger.LoggerFactory;

public class Bar {
    public void sayHello() {
        Logger logger = LoggerFactory.createLogger();
        logger.sendLog("ComponentA: Started org.example.Bar!");
    }
}
