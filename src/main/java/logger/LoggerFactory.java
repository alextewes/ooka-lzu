package logger;

public class LoggerFactory {
    public static Logger createLogger() {
        return new ConsoleLogger();
    }
}

