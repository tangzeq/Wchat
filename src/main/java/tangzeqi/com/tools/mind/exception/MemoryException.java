package tangzeqi.com.tools.mind.exception;

/**
 * 内存异常 - 内存管理相关的异常
 */
public class MemoryException extends MindServiceException {
    public MemoryException() {
        super();
    }

    public MemoryException(String message) {
        super(message);
    }

    public MemoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemoryException(Throwable cause) {
        super(cause);
    }

    protected MemoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
