package tangzeqi.com.tools.mind.exception;

import tangzeqi.com.tools.mind.logging.Logger;
import tangzeqi.com.tools.mind.logging.DefaultLogger;

/**
 * 默认异常处理器实现
 */
public class DefaultExceptionHandler implements ExceptionHandler {
    private final Logger logger;

    public DefaultExceptionHandler() {
        this.logger = new DefaultLogger(DefaultExceptionHandler.class);
    }

    public DefaultExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void handleException(Exception e, String message) {
        // 根据异常类型进行不同的处理
        if (e instanceof MindServiceException) {
            handleMindServiceException((MindServiceException) e, message);
        } else if (e instanceof java.io.IOException) {
            handleIOException((java.io.IOException) e, message);
        } else if (e instanceof SecurityException) {
            handleSecurityException((SecurityException) e, message);
        } else if (e instanceof RuntimeException) {
            handleRuntimeException((RuntimeException) e, message);
        } else {
            logger.error(message, e);
            // 可以添加其他处理逻辑，如发送告警、记录到数据库等
        }
    }

    @Override
    public void handleRuntimeException(RuntimeException e, String message) {
        logger.error(message, e);
        // 可以添加其他处理逻辑，如根据具体的运行时异常类型进行不同处理
    }

    @Override
    public void handleIOException(java.io.IOException e, String message) {
        logger.error(message, e);
        // 可以添加其他处理逻辑，如重试机制
        // 例如，对于文件系统相关的IO异常，可以尝试重新创建目录或文件
    }

    @Override
    public void handleSecurityException(SecurityException e, String message) {
        logger.error(message, e);
        // 可以添加其他处理逻辑，如安全告警
        // 例如，记录安全事件，通知管理员
    }

    /**
     * 处理记忆服务异常
     * @param e 记忆服务异常
     * @param message 错误消息
     */
    public void handleMindServiceException(MindServiceException e, String message) {
        if (e instanceof PersistenceException) {
            handlePersistenceException((PersistenceException) e, message);
        } else if (e instanceof SearchException) {
            handleSearchException((SearchException) e, message);
        } else if (e instanceof MemoryException) {
            handleMemoryException((MemoryException) e, message);
        } else {
            logger.error(message, e);
        }
    }

    /**
     * 处理持久化异常
     * @param e 持久化异常
     * @param message 错误消息
     */
    public void handlePersistenceException(PersistenceException e, String message) {
        logger.error("Persistence error: " + message, e);
        // 可以添加其他处理逻辑，如尝试从备份恢复
    }

    /**
     * 处理搜索异常
     * @param e 搜索异常
     * @param message 错误消息
     */
    public void handleSearchException(SearchException e, String message) {
        logger.error("Search error: " + message, e);
        // 可以添加其他处理逻辑，如清理搜索缓存
    }

    /**
     * 处理内存异常
     * @param e 内存异常
     * @param message 错误消息
     */
    public void handleMemoryException(MemoryException e, String message) {
        logger.error("Memory error: " + message, e);
        // 可以添加其他处理逻辑，如清理内存缓存
    }
}
