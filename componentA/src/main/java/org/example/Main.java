package org.example;

import custom.annotation.component.Component;
import logger.Inject;
import logger.Logger;

public class Main {

    static Bar bar = new Bar();
    static Foo foo = new Foo();

    @Inject
    static Logger logger;

    @Component(Component.Lifecycle.START)
    public void start() {
        logger.sendLog("ComponentA: started.");
        bar.sayHello();
    }

    @Component(Component.Lifecycle.STOP)
    public void stop() {
        foo.sayBye();
        logger.sendLog("ComponentA: stopped.");
    }

    public static void main(String[] args) {

    }

}
