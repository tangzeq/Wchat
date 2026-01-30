package tangzeqi.com.tools.mind.logging;

/**
 * 日志接口 - 提供标准化的日志记录
 */
public interface Logger {
    /**
     * 记录跟踪信息
     * @param message 日志消息
     */
    void trace(String message);

    /**
     * 记录调试信息
     * @param message 日志消息
     */
    void debug(String message);

    /**
     * 记录信息
     * @param message 日志消息
     */
    void info(String message);

    /**
     * 记录警告信息
     * @param message 日志消息
     */
    void warn(String message);

    /**
     * 记录错误信息
     * @param message 日志消息
     */
    void error(String message);

    /**
     * 记录错误信息
     * @param message 日志消息
     * @param throwable 异常对象
     */
    void error(String message, Throwable throwable);
}
