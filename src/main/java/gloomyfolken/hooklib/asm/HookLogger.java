package gloomyfolken.hooklib.asm;

public interface HookLogger {
    void debug(String message);

    void warning(String message);

    void severe(String message);

    void severe(String message, Throwable cause);
}
