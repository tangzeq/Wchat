package tangzeqi.com.flowwork.constracts;

public enum TaskStatus {
    START(0, "进行中"),
    END(0, "已结束"),
    STOP(0, "已暂停"),
    PASS(0, "已通过"),
    REJECT(0, "已驳回"),
    ;

    private int status;
    private String statusName;

    TaskStatus(int status, String statusName) {
        this.status = status;
        this.statusName = statusName;
    }
    public int getStatus() {
        return status;
    }
    public String getStatusName() {
        return statusName;
    }
}
