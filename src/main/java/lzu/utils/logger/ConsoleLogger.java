package lzu.utils.logger;

import java.time.LocalDateTime;

public class ConsoleLogger implements Logger {
    @Override
    public void sendLog(String str) {
        System.out.println("++++ LOG: " + str + " (" + LocalDateTime.now() + ")");
    }
}
