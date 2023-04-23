package org.example;

import custom.annotation.component.Component;

public class Main {

    static Bar bar = new Bar();
    static Foo foo = new Foo();

    @Component(Component.Lifecycle.START)
    public void start() {
        System.out.println("MyComponentB started.");
        bar.sayHello();
    }

    @Component(Component.Lifecycle.STOP)
    public void stop() {
        foo.sayBye();
        System.out.println("MyComponentB stopped.");
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }




}
