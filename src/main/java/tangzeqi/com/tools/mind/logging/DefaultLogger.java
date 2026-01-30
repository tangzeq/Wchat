package tangzeqi.com.tools.mind.logging;

import org.slf4j.LoggerFactory;

/**
 * 默认日志实现 - 使用SLF4J
 */
public class DefaultLogger implements Logger {
    private final org.slf4j.Logger logger;

    public DefaultLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public DefaultLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
