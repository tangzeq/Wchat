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
    
    public MindProgressUIListener(JTextArea mindOutputArea) {
        this.mindOutputArea = mindOutputArea;
        this.currentQuery = "";
    }
    
    @Override
    public void onStart(String query) {
        this.currentQuery = query;
        updateUI("🔍 开始查找: 【" + query + "】相关信息\n");
    }
    @Override
    public void onSearchProgress(int totalFiles,  int totalLines) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            lastUpdateTime = currentTime;
            updateUI("🔍 开始查找: 【" + currentQuery + "】相关信息\n📁 处理进度: " + totalFiles + " 个文件\n📊 已处理: " + totalLines + " 行\n");
        }
    }

    @Override
    public void onComplete(List<ScoredEntry> results,int fileCount, int memoryCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 开始查找: 【").append(currentQuery).append("】相关信息\n");
        sb.append("✅ 查找结束: \n");
        sb.append("📁 共查找了 "+fileCount+" 个持久化文件\n");
        sb.append("💭 搜索了 "+memoryCount+" 条记忆\n\n");
        String[] top = new String[]{"🥇","🥈", "🥉","🔸" };
        for (int i = 0; i < results.size(); i++) {
            ScoredEntry entry = results.get(i);
            sb.append( top[i>3?3:i]+entry.getContent() + "\n【匹配度："+entry.getScore()+"】\n");
        }

        updateUI(sb.toString());
    }
    @Override
    public void onSave(int fileCount, int memoryCount,boolean save) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 开始查找: 【").append(currentQuery).append("】相关信息\n");
        sb.append("✅ 查找结束: \n");
        sb.append("📁 共查找了 "+fileCount+" 个持久化文件\n");
        sb.append("💭 搜索了 "+memoryCount+" 条记忆\n\n");
        if(save){
            sb.append("📝 未找到重复记忆，保存成功\n");
        } else {
            sb.append("🗑️ 存在重复记忆，已忽略\n");
        }
        updateUI(sb.toString());
    }

    private void updateUI(String message) {
        if (mindOutputArea != null) {
            SwingUtilities.invokeLater(() -> {
                mindOutputArea.setText(message);

            });
        }
    }
}
