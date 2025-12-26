package tangzeqi.com.flowwork.service;

import tangzeqi.com.flowwork.definition.Task;

public interface IAction {
    //任务开始
    void start(Task task);
    //任务结束
    void end(Task task);
    //任务暂停
    void stop(Task task);
    //任务通过
    void pass(Task task);
    //任务拒绝
    void reject(Task task);
}
