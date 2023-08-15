package lzu.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class UniqueIdGenerator {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    public static int generateNewId() {
        return ID_COUNTER.incrementAndGet();
    }
}
