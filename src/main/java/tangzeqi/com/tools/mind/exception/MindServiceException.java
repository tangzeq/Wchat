package tangzeqi.com.tools.mind.exception;

/**
 * 记忆服务异常 - 基础异常类
 */
public class MindServiceException extends Exception {
    public MindServiceException() {
        super();
    }

    public MindServiceException(String message) {
        super(message);
    }

    public MindServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MindServiceException(Throwable cause) {
        super(cause);
    }

    protected MindServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
