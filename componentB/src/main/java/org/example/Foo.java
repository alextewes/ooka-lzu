package org.example;

import logger.Logger;
import logger.LoggerFactory;

public class Foo {

    public void sayBye() {
        Logger logger = LoggerFactory.createLogger();
        logger.sendLog("ComponentB: Stopped org.example.Foo!");
    }

}
