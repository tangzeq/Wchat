package tangzeqi.com.tools.mind.server;

import java.util.List;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * 记忆库进度监听器实现，负责UI进度更新
 */
public class MindProgressUIListener implements MindProgressListener {
    private final JTextArea mindOutputArea;
    private String currentQuery;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 100; // 100ms更新一次进度
    private final StringBuilder stringBuilder; // 可重用的StringBuilder
    
    public MindProgressUIListener(JTextArea mindOutputArea) {
        this.mindOutputArea = mindOutputArea;
        this.currentQuery = "";
        this.stringBuilder = new StringBuilder(2048); // 预分配足够的容量
    }
    
    @Override
    public void onStart(String query) {
        this.currentQuery = query;
        resetStringBuilder();
        stringBuilder.append("🔍 开始查找: 【").append(query).append("】相关信息\n");
        updateUI(stringBuilder.toString());
    }
    @Override
    public void onSearchProgress(int totalFiles,  int totalLines) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            lastUpdateTime = currentTime;
            resetStringBuilder();
            stringBuilder.append("🔍 开始查找: 【").append(currentQuery).append("】相关信息\n");
            stringBuilder.append("📁 处理进度: ").append(totalFiles).append(" 个文件\n");
            stringBuilder.append("📊 已处理: " ).append(totalLines).append(" 行\n");
            updateUI(stringBuilder.toString());
        }
    }

    @Override
    public void onComplete(List<ScoredEntry> results,int fileCount, int memoryCount) {
        resetStringBuilder();
        stringBuilder.append("🔍 开始查找: 【").append(currentQuery).append("】相关信息\n");
        stringBuilder.append("✅ 查找结束: \n");
        stringBuilder.append("📁 共查找了 ").append(fileCount).append(" 个持久化文件\n");
        stringBuilder.append("💭 搜索了 ").append(memoryCount).append(" 条记忆\n\n");
        
        String[] top = new String[]{"🥇","🥈", "🥉","🔸" };
        for (int i = 0; i < results.size(); i++) {
            ScoredEntry entry = results.get(i);
            stringBuilder.append(top[i>3?3:i]).append(entry.getContent()).append("\n");
            stringBuilder.append("【匹配度：").append(entry.getScore()).append("】\n");
        }

        updateUI(stringBuilder.toString());
    }
    @Override
    public void onSave(int fileCount, int memoryCount,boolean save) {
        resetStringBuilder();
        stringBuilder.append("🔍 开始查找: 【").append(currentQuery).append("】相关信息\n");
        stringBuilder.append("✅ 查找结束: \n");
        stringBuilder.append("📁 共查找了 ").append(fileCount).append(" 个持久化文件\n");
        stringBuilder.append("💭 搜索了 ").append(memoryCount).append(" 条记忆\n\n");
        
        if(save){
            stringBuilder.append("📝 未找到重复记忆，保存成功\n");
        } else {
            stringBuilder.append("🗑️ 存在重复记忆，已忽略\n");
        }
        
        updateUI(stringBuilder.toString());
    }

    private void updateUI(String message) {
        if (mindOutputArea != null) {
            SwingUtilities.invokeLater(() -> {
                mindOutputArea.setText(message);
            });
        }
    }
    
    /**
     * 重置StringBuilder，用于重用
     */
    private void resetStringBuilder() {
        stringBuilder.setLength(0);
    }
}
