package gloomyfolken.hooklib.asm;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class HookLoggerManager {
    private static HookLogger logger;

    public static HookLogger getLogger() {
        if (logger == null)
            logger = new SystemOutLogger();
        return logger;
    }

    public static void setLogger(HookLogger logger) {
        HookLoggerManager.logger = logger;
    }

    public static class SystemOutLogger implements HookLogger {

        @Override
        public void debug(String message) {
            System.out.println("[DEBUG] " + message);
        }

        @Override
        public void warning(String message) {
            System.out.println("[WARNING] " + message);
        }

        @Override
        public void severe(String message) {
            System.out.println("[SEVERE] " + message);
        }

        @Override
        public void severe(String message, Throwable cause) {
            severe(message);
            cause.printStackTrace();
        }
    }

    public static class VanillaLogger implements HookLogger {
        private final Logger logger;

        public VanillaLogger(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void debug(String message) {
            logger.fine(message);
        }

        @Override
        public void warning(String message) {
            logger.warning(message);
        }

        @Override
        public void severe(String message) {
            logger.severe(message);
        }

        @Override
        public void severe(String message, Throwable cause) {
            logger.log(Level.SEVERE, message, cause);
        }
    }
}
