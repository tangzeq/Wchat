package tangzeqi.com.tools.mind.service;

import tangzeqi.com.tools.mind.MindService;
import tangzeqi.com.tools.mind.core.MemoryManager;
import tangzeqi.com.tools.mind.core.DefaultMemoryManager;
import tangzeqi.com.tools.mind.core.SearchEngine;
import tangzeqi.com.tools.mind.core.DefaultSearchEngine;
import tangzeqi.com.tools.mind.core.PersistenceManager;
import tangzeqi.com.tools.mind.core.DefaultPersistenceManager;
import tangzeqi.com.tools.mind.core.SearchResult;
import tangzeqi.com.tools.mind.storage.MemoryEntry;
import tangzeqi.com.tools.mind.logging.Logger;
import tangzeqi.com.tools.mind.logging.DefaultLogger;
import tangzeqi.com.tools.mind.exception.ExceptionHandler;
import tangzeqi.com.tools.mind.exception.DefaultExceptionHandler;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认记忆服务实现
 */
public class DefaultMindService implements MindService {
    private final MemoryManager memoryManager;
    private final SearchEngine searchEngine;
    private final PersistenceManager persistenceManager;
    private final Logger logger;
    private final ExceptionHandler exceptionHandler;
    private boolean isRunning;
    private long lastStorageModifiedTime;

    public DefaultMindService() {
        this.memoryManager = new DefaultMemoryManager();
        this.searchEngine = new DefaultSearchEngine(memoryManager);
        this.persistenceManager = new DefaultPersistenceManager();
        this.logger = new DefaultLogger(DefaultMindService.class);
        this.exceptionHandler = new DefaultExceptionHandler(logger);
        this.isRunning = true;
        this.lastStorageModifiedTime = getCurrentStorageModifiedTime();
        
        // 加载数据
        loadData();
    }

    public DefaultMindService(MemoryManager memoryManager, SearchEngine searchEngine,
                             PersistenceManager persistenceManager, Logger logger,
                             ExceptionHandler exceptionHandler) {
        this.memoryManager = memoryManager;
        this.searchEngine = searchEngine;
        this.persistenceManager = persistenceManager;
        this.logger = logger;
        this.exceptionHandler = exceptionHandler;
        this.isRunning = true;
        this.lastStorageModifiedTime = getCurrentStorageModifiedTime();
        
        // 加载数据
        loadData();
    }

    private void loadData() {
        try {
            List<MemoryEntry> entries = persistenceManager.load();
            // 清理重复记忆
            List<MemoryEntry> uniqueEntries = removeDuplicateEntries(entries);
            for (MemoryEntry entry : uniqueEntries) {
                memoryManager.addMemory(entry.getContent(), entry.getCategory());
            }
            // 如果清理了重复记忆，保存清理后的数据
            if (uniqueEntries.size() < entries.size()) {
                saveData();
                logger.info("Removed " + (entries.size() - uniqueEntries.size()) + " duplicate memories during loading");
            }
            logger.info("Loaded " + uniqueEntries.size() + " memories from storage");
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to load data from storage");
            
            // 尝试从备份恢复
            try {
                boolean recovered = persistenceManager.recoverFromBackup();
                if (recovered) {
                    List<MemoryEntry> entries = persistenceManager.load();
                    // 清理重复记忆
                    List<MemoryEntry> uniqueEntries = removeDuplicateEntries(entries);
                    for (MemoryEntry entry : uniqueEntries) {
                        memoryManager.addMemory(entry.getContent(), entry.getCategory());
                    }
                    // 如果清理了重复记忆，保存清理后的数据
                    if (uniqueEntries.size() < entries.size()) {
                        saveData();
                        logger.info("Removed " + (entries.size() - uniqueEntries.size()) + " duplicate memories during recovery");
                    }
                    logger.info("Recovered " + uniqueEntries.size() + " memories from backup");
                }
            } catch (Exception ex) {
                exceptionHandler.handleException(ex, "Failed to recover from backup");
            }
        }
    }
    
    /**
     * 清理重复的记忆条目
     * @param entries 记忆条目列表
     * @return 去重后的记忆条目列表
     */
    private List<MemoryEntry> removeDuplicateEntries(List<MemoryEntry> entries) {
        Set<String> contentSet = new java.util.HashSet<>();
        List<MemoryEntry> uniqueEntries = new java.util.ArrayList<>();
        
        for (MemoryEntry entry : entries) {
            if (!contentSet.contains(entry.getContent())) {
                contentSet.add(entry.getContent());
                uniqueEntries.add(entry);
            }
        }
        
        return uniqueEntries;
    }
    
    /**
     * 清理持久化存储中的重复记忆
     */
    private void cleanupDuplicateMemories() {
        try {
            List<MemoryEntry> entries = persistenceManager.load();
            List<MemoryEntry> uniqueEntries = removeDuplicateEntries(entries);
            
            if (uniqueEntries.size() < entries.size()) {
                persistenceManager.save(uniqueEntries);
                logger.info("Cleaned up " + (entries.size() - uniqueEntries.size()) + " duplicate memories from storage");
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to cleanup duplicate memories");
        }
    }

    private void saveData() {
        try {
            List<MemoryEntry> entries = memoryManager.getAllMemories();
            persistenceManager.save(entries);
            logger.info("Saved " + entries.size() + " memories to storage");
            // 更新最后修改时间
            this.lastStorageModifiedTime = getCurrentStorageModifiedTime();
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to save data to storage");
        }
    }

    /**
     * 获取当前存储文件的最后修改时间
     * @return 最后修改时间戳
     */
    private long getCurrentStorageModifiedTime() {
        try {
            if (persistenceManager instanceof DefaultPersistenceManager) {
                DefaultPersistenceManager defaultPersistenceManager = (DefaultPersistenceManager) persistenceManager;
                java.nio.file.Path storagePath = defaultPersistenceManager.getStoragePath();
                java.nio.file.Path storageDir = storagePath.getParent();
                tangzeqi.com.tools.mind.storage.StorageEngine storageEngine = defaultPersistenceManager.getStorageEngine();
                
                long latestModifiedTime = 0;
                int fileCount = 0;
                
                // 检查主存储文件
                if (storageEngine.exists(storagePath)) {
                    long mainFileTime = storageEngine.lastModified(storagePath);
                    if (mainFileTime > latestModifiedTime) {
                        latestModifiedTime = mainFileTime;
                    }
                    fileCount++;
                }
                
                // 检查存储目录下的所有 JSON 文件
                if (storageDir != null && storageEngine.exists(storageDir)) {
                    List<java.nio.file.Path> jsonFiles = storageEngine.list(storageDir, "*.json");
                    for (java.nio.file.Path file : jsonFiles) {
                        if (!file.equals(storagePath)) {
                            long fileTime = storageEngine.lastModified(file);
                            if (fileTime > latestModifiedTime) {
                                latestModifiedTime = fileTime;
                            }
                            fileCount++;
                        }
                    }
                }
                
                // 结合文件数量和最新修改时间，确保文件新增或删除时也能检测到变化
                // 使用 fileCount * 1000000000L 来确保文件数量变化时，返回值会大于之前的最新修改时间
                return latestModifiedTime + (fileCount * 1000000000L);
            }
        } catch (Exception e) {
            logger.error("Failed to get storage modified time: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 检查存储文件是否变更，如果变更则重新加载
     */
    private void checkStorageChanges() {
        try {
            long currentModifiedTime = getCurrentStorageModifiedTime();
            if (currentModifiedTime > lastStorageModifiedTime) {
                logger.info("Storage file has changed, reloading data...");
                // 清除当前内存中的数据
                memoryManager.clearAllMemories();
                // 重新加载数据
                loadData();
                // 更新最后修改时间
                this.lastStorageModifiedTime = currentModifiedTime;
                // 清除搜索引擎缓存
                if (searchEngine instanceof DefaultSearchEngine) {
                    ((DefaultSearchEngine) searchEngine).clearAllCaches();
                }
                logger.info("Data reloaded successfully");
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to check storage changes");
        }
    }

    @Override
    public String set(String mind) {
        if (!isRunning) {
            return "Service is shutdown";
        }
        
        if (mind == null || mind.trim().isEmpty()) {
            return "Text cannot be empty";
        }
        
        try {
            // 重新加载持久化文件
            reloadData();
            
            // 清理持久化存储中的重复记忆
            cleanupDuplicateMemories();
            
            // 检查是否已存在相同内容的记忆
            if (memoryManager.containsMemoryContent(mind)) {
                // 直接忽略重复记忆
                return "Memory already exists, skipped: " + mind;
            }
            
            String id = memoryManager.addMemory(mind, "default");
            saveData();
            
            // 业务结束后立即释放内存
            memoryManager.clearAllMemories();
            
            return "Added memory with id: " + id;
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to add memory");
            return "Failed to add memory: " + e.getMessage();
        }
    }

    public String set(String text, String category) {
        if (!isRunning) {
            return "Service is shutdown";
        }
        
        if (text == null || text.trim().isEmpty()) {
            return "Text cannot be empty";
        }
        
        try {
            // 重新加载持久化文件
            reloadData();
            
            // 清理持久化存储中的重复记忆
            cleanupDuplicateMemories();
            
            // 检查是否已存在相同内容的记忆
            if (memoryManager.containsMemoryContent(text)) {
                // 直接忽略重复记忆
                return "Memory already exists, skipped: " + text;
            }
            
            String id = memoryManager.addMemory(text, category != null ? category : "default");
            saveData();
            
            // 业务结束后立即释放内存
            memoryManager.clearAllMemories();
            
            return "Added memory with id: " + id;
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to add memory");
            return "Failed to add memory: " + e.getMessage();
        }
    }

    @Override
    public List<String> get(String query) {
        if (!isRunning) {
            return List.of("Service is shutdown");
        }
        
        // 重新加载持久化文件
        reloadData();
        
        List<String> results = search(query, null, 10, true);
        
        // 业务结束后立即释放内存
        memoryManager.clearAllMemories();
        
        return results;
    }

    public List<String> getByCategory(String query, String category) {
        if (!isRunning) {
            return List.of("Service is shutdown");
        }
        
        // 重新加载持久化文件
        reloadData();
        
        List<String> results = search(query, category, 10, true);
        
        // 业务结束后立即释放内存
        memoryManager.clearAllMemories();
        
        return results;
    }

    public List<String> search(String query, String category, int limit, boolean enableFuzzySearch) {
        if (!isRunning) {
            return List.of("Service is shutdown");
        }
        
        try {
            List<SearchResult> results = searchEngine.search(query, category, limit, enableFuzzySearch);
            return results.stream()
                    .map(SearchResult::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to search memories");
            return List.of("Failed to search memories: " + e.getMessage());
        }
    }

    /**
     * 重新加载持久化文件
     */
    private void reloadData() {
        try {
            // 清除当前内存中的数据
            memoryManager.clearAllMemories();
            // 重新加载数据
            loadData();
            // 清除搜索引擎缓存
            if (searchEngine instanceof DefaultSearchEngine) {
                ((DefaultSearchEngine) searchEngine).clearAllCaches();
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to reload data");
        }
    }

    public boolean delete(String id) {
        if (!isRunning) {
            return false;
        }
        
        try {
            boolean deleted = memoryManager.deleteMemory(id);
            if (deleted) {
                saveData();
            }
            return deleted;
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to delete memory");
            return false;
        }
    }

    public void clear() {
        if (!isRunning) {
            return;
        }
        
        try {
            memoryManager.clearAllMemories();
            saveData();
            logger.info("Cleared all memories");
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to clear memories");
        }
    }

    public int size() {
        if (!isRunning) {
            return 0;
        }
        
        try {
            return memoryManager.getMemoryCount();
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to get memory count");
            return 0;
        }
    }

    public boolean isEmpty() {
        if (!isRunning) {
            return true;
        }
        
        try {
            return memoryManager.isEmpty();
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to check if memories is empty");
            return true;
        }
    }

    public void shutdown() {
        if (!isRunning) {
            return;
        }
        
        try {
            saveData();
            logger.info("Shutting down MindService");
            isRunning = false;
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Failed to shutdown MindService");
        }
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
