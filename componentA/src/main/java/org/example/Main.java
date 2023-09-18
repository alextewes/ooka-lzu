package org.example;

import lzu.utils.Component;
import lzu.utils.logger.ConsoleLogger;
import lzu.utils.logger.Inject;
import lzu.utils.MessageQueue;

import java.util.ArrayList;
import java.util.List;

public class Main {

    @Inject
    static ConsoleLogger logger;

    @Inject
    static MessageQueue messageQueue;

    @Component(Component.Lifecycle.START)
    public void start() {
        logger.sendLog("ComponentA: started.");
        List<Integer> primes = calculatePrimes(100);
        String message = primes.toString();
        messageQueue.sendMessage(message);
        logger.sendLog("ComponentA sent Message: " + message);
    }

    @Component(Component.Lifecycle.STOP)
    public void stop() {
        logger.sendLog("ComponentA: stopped.");
    }

    public static List<Integer> calculatePrimes(int limit) {
        boolean[] isPrime = new boolean[limit + 1];
        List<Integer> primeList = new ArrayList<>();
        for (int i = 2; i <= limit; i++) {
            isPrime[i] = true;
        }
        for (int divisor = 2; divisor * divisor <= limit; divisor++) {
            if (isPrime[divisor]) {
                for (int i = divisor * divisor; i <= limit; i += divisor) {
                    isPrime[i] = false;
                }
            }
        }
        for (int i = 2; i <= limit; i++) {
            if (isPrime[i]) {
                primeList.add(i);
            }
        }
        return primeList;
    }

    public static void main(String[] args) {
    }

}
