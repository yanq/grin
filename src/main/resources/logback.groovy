import ch.qos.logback.classic.encoder.PatternLayoutEncoder

statusListener(OnConsoleStatusListener)

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%21.-21thread] %-5level %-30.30logger{29} - %msg%n"
    }
}

root(DEBUG, ["CONSOLE"])