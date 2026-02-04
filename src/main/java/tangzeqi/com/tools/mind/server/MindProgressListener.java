package tangzeqi.com.tools.mind.server;

import java.util.List;

public interface MindProgressListener {
    /**
     * 开始查找
     * @param query 查询内容
     */
    void onStart(String query);

    /**
     * 查找时进度更新
     * @param currentFiles 当前处理的文件数
     * @param currentLines 当前处理的行数
     */
    void onSearchProgress(int currentFiles, int currentLines);

    /**
     * 查找结束显示搜索结果
     * @param fileCount 处理的文件数
     * @param memoryCount 处理的记忆数
     */
    void onComplete(List<ScoredEntry> results,int fileCount, int memoryCount);

    /**
     * 查找结束显示搜索结果
     * @param fileCount 处理的文件数
     * @param memoryCount 处理的记忆数
     */
    void onSave(int fileCount, int memoryCount,boolean save);

}