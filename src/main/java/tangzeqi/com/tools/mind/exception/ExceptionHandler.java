package tangzeqi.com.tools.mind.exception;

/**
 * 异常处理器接口 - 统一的异常处理
 */
public interface ExceptionHandler {
    /**
     * 处理异常
     * @param e 异常对象
     * @param message 错误消息
     */
    void handleException(Exception e, String message);

    /**
     * 处理运行时异常
     * @param e 运行时异常对象
     * @param message 错误消息
     */
    void handleRuntimeException(RuntimeException e, String message);

    /**
     * 处理IO异常
     * @param e IO异常对象
     * @param message 错误消息
     */
    void handleIOException(java.io.IOException e, String message);

    /**
     * 处理安全异常
     * @param e 安全异常对象
     * @param message 错误消息
     */
    void handleSecurityException(SecurityException e, String message);
}
