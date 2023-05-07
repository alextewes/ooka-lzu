package org.example;

import logger.Logger;
import logger.LoggerFactory;

public class Foo {

    public void sayBye() {
        Logger logger = LoggerFactory.createLogger();
        logger.sendLog("ComponentA: Stopped org.example.Foo!");
    }

}
