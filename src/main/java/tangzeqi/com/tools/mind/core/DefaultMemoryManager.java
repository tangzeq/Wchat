package tangzeqi.com.tools.mind.core;

import tangzeqi.com.tools.mind.storage.MemoryEntry;
import tangzeqi.com.tools.mind.storage.DataStore;
import tangzeqi.com.tools.mind.storage.InMemoryDataStore;
import tangzeqi.com.tools.mind.analysis.SemanticAnalyzer;
import tangzeqi.com.tools.mind.analysis.DefaultSemanticAnalyzer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 默认内存管理器实现
 */
public class DefaultMemoryManager implements MemoryManager {
    private final DataStore dataStore;
    private final SemanticAnalyzer semanticAnalyzer;
    private final Map<String, Integer> documentFrequency;

    public DefaultMemoryManager() {
        this.dataStore = new InMemoryDataStore();
        this.semanticAnalyzer = new DefaultSemanticAnalyzer();
        this.documentFrequency = new java.util.HashMap<>();
    }

    public DefaultMemoryManager(DataStore dataStore, SemanticAnalyzer semanticAnalyzer) {
        this.dataStore = dataStore;
        this.semanticAnalyzer = semanticAnalyzer;
        this.documentFrequency = new java.util.HashMap<>();
        // 初始化文档频率
        initializeDocumentFrequency();
    }
    
    /**
     * 初始化文档频率
     */
    private void initializeDocumentFrequency() {
        List<MemoryEntry> allMemories = dataStore.getAll();
        for (MemoryEntry entry : allMemories) {
            // 从TF-IDF向量中提取关键词
            for (String keyword : entry.getTfidfVector().keySet()) {
                documentFrequency.put(keyword, documentFrequency.getOrDefault(keyword, 0) + 1);
            }
        }
    }

    @Override
    public String addMemory(String content, String category) {
        // 提取关键词
        List<String> keywords = semanticAnalyzer.extractKeywords(content, 100);
        
        // 计算词频
        Map<String, Integer> termFrequency = semanticAnalyzer.calculateTermFrequency(keywords);
        
        // 计算文档频率
        updateDocumentFrequency(keywords);
        
        // 构建TF-IDF向量
        Map<String, Double> tfidfVector = semanticAnalyzer.buildTfidfVector(
                termFrequency, documentFrequency, dataStore.size() + 1);
        
        // 创建记忆条目
        MemoryEntry entry = new MemoryEntry(content, category, tfidfVector);
        
        // 添加到数据存储
        dataStore.add(entry);
        
        return entry.getId();
    }

    @Override
    public Optional<MemoryEntry> getMemory(String id) {
        return dataStore.get(id);
    }

    @Override
    public Optional<MemoryEntry> getMemoryByContent(String content) {
        return dataStore.getByContent(content);
    }

    @Override
    public List<MemoryEntry> getAllMemories() {
        return dataStore.getAll();
    }

    @Override
    public List<MemoryEntry> getMemoriesByCategory(String category) {
        return dataStore.getByCategory(category);
    }

    @Override
    public boolean updateMemory(String id, String content, String category) {
        Optional<MemoryEntry> existingEntry = dataStore.get(id);
        if (!existingEntry.isPresent()) {
            return false;
        }
        
        // 提取关键词
        List<String> keywords = semanticAnalyzer.extractKeywords(content, 100);
        
        // 计算词频
        Map<String, Integer> termFrequency = semanticAnalyzer.calculateTermFrequency(keywords);
        
        // 更新文档频率
        updateDocumentFrequency(keywords);
        
        // 构建TF-IDF向量
        Map<String, Double> tfidfVector = semanticAnalyzer.buildTfidfVector(
                termFrequency, documentFrequency, dataStore.size());
        
        // 创建新的记忆条目
        MemoryEntry entry = new MemoryEntry(
                id, content, category, tfidfVector,
                existingEntry.get().getCreatedAt(),
                System.currentTimeMillis(),
                existingEntry.get().getAccessCount()
        );
        
        // 更新数据存储
        return dataStore.update(entry);
    }

    @Override
    public boolean deleteMemory(String id) {
        Optional<MemoryEntry> entry = dataStore.get(id);
        if (!entry.isPresent()) {
            return false;
        }
        
        // 更新文档频率
        decrementDocumentFrequency(entry.get().getTfidfVector().keySet());
        
        // 从数据存储中删除
        return dataStore.delete(id);
    }

    @Override
    public void clearAllMemories() {
        dataStore.clear();
        documentFrequency.clear();
    }

    @Override
    public int getMemoryCount() {
        return dataStore.size();
    }

    @Override
    public boolean isEmpty() {
        return dataStore.isEmpty();
    }

    @Override
    public boolean containsMemory(String id) {
        return dataStore.contains(id);
    }

    @Override
    public boolean containsMemoryContent(String content) {
        return dataStore.containsContent(content);
    }

    @Override
    public DataStore getDataStore() {
        return dataStore;
    }

    private void updateDocumentFrequency(List<String> keywords) {
        // 使用集合去重，避免重复计算
        java.util.Set<String> uniqueKeywords = new java.util.HashSet<>(keywords);
        for (String keyword : uniqueKeywords) {
            documentFrequency.put(keyword, documentFrequency.getOrDefault(keyword, 0) + 1);
        }
    }

    private void decrementDocumentFrequency(java.util.Set<String> keywords) {
        for (String keyword : keywords) {
            Integer count = documentFrequency.get(keyword);
            if (count != null && count > 0) {
                documentFrequency.put(keyword, count - 1);
            }
        }
    }
}
