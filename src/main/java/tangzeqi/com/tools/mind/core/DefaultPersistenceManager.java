package tangzeqi.com.tools.mind.core;

import tangzeqi.com.tools.mind.storage.StorageEngine;
import tangzeqi.com.tools.mind.storage.FileStorageEngine;
import tangzeqi.com.tools.mind.storage.MemoryEntry;
import tangzeqi.com.tools.mind.logging.Logger;
import tangzeqi.com.tools.mind.logging.DefaultLogger;
import tangzeqi.com.tools.mind.exception.PersistenceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认持久化管理器实现
 */
public class DefaultPersistenceManager implements PersistenceManager {
    private Path storagePath;
    private Path backupPath;
    private StorageEngine storageEngine;
    private int maxBackups;
    private int backupIntervalHours;
    private long lastBackupTime;
    private final ObjectMapper objectMapper;
    private final Logger logger;

    public DefaultPersistenceManager() {
        this.storagePath = Paths.get(System.getProperty("user.home"), ".mind-idea-plugin", "memory.json");
        this.backupPath = Paths.get(System.getProperty("user.home"), ".mind-idea-plugin", "backups");
        this.storageEngine = new FileStorageEngine();
        this.maxBackups = 5;
        this.backupIntervalHours = 24;
        this.lastBackupTime = 0;
        this.objectMapper = new ObjectMapper();
        // 禁用缩进输出，确保每个MemoryEntry对象占一行
        this.objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        this.logger = new DefaultLogger(DefaultPersistenceManager.class);
    }

    public DefaultPersistenceManager(Path storagePath, Path backupPath, StorageEngine storageEngine,
                                    int maxBackups, int backupIntervalHours) {
        this.storagePath = storagePath;
        this.backupPath = backupPath;
        this.storageEngine = storageEngine;
        this.maxBackups = maxBackups;
        this.backupIntervalHours = backupIntervalHours;
        this.lastBackupTime = 0;
        this.objectMapper = new ObjectMapper();
        // 禁用缩进输出，确保每个MemoryEntry对象占一行
        this.objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        this.logger = new DefaultLogger(DefaultPersistenceManager.class);
    }

    public DefaultPersistenceManager(Path storagePath, Path backupPath, StorageEngine storageEngine,
                                    int maxBackups, int backupIntervalHours, Logger logger) {
        this.storagePath = storagePath;
        this.backupPath = backupPath;
        this.storageEngine = storageEngine;
        this.maxBackups = maxBackups;
        this.backupIntervalHours = backupIntervalHours;
        this.lastBackupTime = 0;
        this.objectMapper = new ObjectMapper();
        // 禁用缩进输出，确保每个MemoryEntry对象占一行
        this.objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        this.logger = logger;
    }

    @Override
    public void save(List<MemoryEntry> entries) throws PersistenceException {
        if (entries.isEmpty()) {
            return;
        }

        try {
            // 确保目录存在
            storageEngine.createDirectories(storagePath.getParent());

            // 序列化记忆条目
            List<String> content = new ArrayList<>();
            for (MemoryEntry entry : entries) {
                content.add(objectMapper.writeValueAsString(entry));
            }

            // 使用原子操作写入文件
            storageEngine.writeAtomic(storagePath, content);

            // 检查是否需要备份
            if (System.currentTimeMillis() - lastBackupTime > backupIntervalHours * 60 * 60 * 1000) {
                backup();
            }
        } catch (Exception e) {
            throw new PersistenceException("Failed to save data to storage", e);
        }
    }

    @Override
    public List<MemoryEntry> load() throws PersistenceException {
        List<MemoryEntry> allEntries = new ArrayList<>();

        try {
            // 加载主存储文件
            if (storageEngine.exists(storagePath)) {
                List<String> content = storageEngine.read(storagePath);
                for (String line : content) {
                    try {
                        MemoryEntry entry = objectMapper.readValue(line, MemoryEntry.class);
                        allEntries.add(entry);
                    } catch (Exception e) {
                        logger.error("Failed to parse memory entry from main file: " + e.getMessage());
                    }
                }
            }

            // 加载目录下的所有其他 JSON 文件
            Path storageDir = storagePath.getParent();
            if (storageDir != null && storageEngine.exists(storageDir)) {
                List<Path> jsonFiles = storageEngine.list(storageDir, "*.json");
                for (Path jsonFile : jsonFiles) {
                    // 跳过主存储文件
                    if (!jsonFile.equals(storagePath)) {
                        try {
                            List<String> content = storageEngine.read(jsonFile);
                            for (String line : content) {
                                try {
                                    MemoryEntry entry = objectMapper.readValue(line, MemoryEntry.class);
                                    allEntries.add(entry);
                                } catch (Exception e) {
                                    logger.error("Failed to parse memory entry from " + jsonFile.getFileName() + ": " + e.getMessage());
                                }
                            }
                            logger.info("Loaded memories from: " + jsonFile.getFileName());
                        } catch (Exception e) {
                            logger.error("Failed to read file: " + jsonFile.getFileName() + ", error: " + e.getMessage());
                        }
                    }
                }
            }

            return allEntries;
        } catch (Exception e) {
            throw new PersistenceException("Failed to load data from storage", e);
        }
    }

    @Override
    public void backup() throws PersistenceException {
        if (!storageEngine.exists(storagePath)) {
            return;
        }

        try {
            // 确保备份目录存在
            storageEngine.createDirectories(backupPath);

            // 生成备份文件名
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Path backupFilePath = backupPath.resolve("memory_backup_" + timestamp + ".json");

            // 复制存储文件到备份目录
            storageEngine.copy(storagePath, backupFilePath);

            // 更新上次备份时间
            lastBackupTime = System.currentTimeMillis();

            logger.info("Created backup: " + backupFilePath.getFileName());

            // 清理旧备份
            cleanupOldBackups();
        } catch (Exception e) {
            throw new PersistenceException("Failed to create backup", e);
        }
    }

    @Override
    public boolean recoverFromBackup() throws PersistenceException {
        try {
            // 列出所有备份文件
            List<Path> backupFiles = storageEngine.list(backupPath);
            if (backupFiles.isEmpty()) {
                logger.warn("No backup files found for recovery");
                return false;
            }

            // 按修改时间排序（最新的在前）
            backupFiles.sort((p1, p2) -> {
                try {
                    return Long.compare(storageEngine.lastModified(p2), storageEngine.lastModified(p1));
                } catch (Exception e) {
                    logger.error("Error comparing backup files: " + e.getMessage());
                    return 0;
                }
            });

            // 选择最新的备份文件
            Path latestBackup = backupFiles.get(0);

            // 复制备份文件到存储路径
            storageEngine.copy(latestBackup, storagePath);

            logger.info("Recovered from backup: " + latestBackup.getFileName());

            return true;
        } catch (Exception e) {
            throw new PersistenceException("Failed to recover from backup", e);
        }
    }

    @Override
    public void cleanupOldBackups() throws PersistenceException {
        try {
            // 列出所有备份文件
            List<Path> backupFiles = storageEngine.list(backupPath);
            if (backupFiles.size() <= maxBackups) {
                return;
            }

            // 按修改时间排序（最新的在前）
            backupFiles.sort((p1, p2) -> {
                try {
                    return Long.compare(storageEngine.lastModified(p2), storageEngine.lastModified(p1));
                } catch (Exception e) {
                    logger.error("Error comparing backup files: " + e.getMessage());
                    return 0;
                }
            });

            // 删除超出限制的旧备份
            int deletedCount = 0;
            for (int i = maxBackups; i < backupFiles.size(); i++) {
                Path backupFile = backupFiles.get(i);
                storageEngine.delete(backupFile);
                deletedCount++;
            }

            if (deletedCount > 0) {
                logger.info("Cleaned up " + deletedCount + " old backup files");
            }
        } catch (Exception e) {
            throw new PersistenceException("Failed to cleanup old backups", e);
        }
    }

    @Override
    public Path getStoragePath() {
        return storagePath;
    }

    @Override
    public Path getBackupPath() {
        return backupPath;
    }

    @Override
    public void setStoragePath(Path storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public void setBackupPath(Path backupPath) {
        this.backupPath = backupPath;
    }

    @Override
    public StorageEngine getStorageEngine() {
        return storageEngine;
    }

    @Override
    public void setStorageEngine(StorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    @Override
    public int getMaxBackups() {
        return maxBackups;
    }

    @Override
    public void setMaxBackups(int maxBackups) {
        this.maxBackups = maxBackups;
    }

    @Override
    public int getBackupIntervalHours() {
        return backupIntervalHours;
    }

    @Override
    public void setBackupIntervalHours(int backupIntervalHours) {
        this.backupIntervalHours = backupIntervalHours;
    }

    @Override
    public long getLastBackupTime() {
        return lastBackupTime;
    }

    @Override
    public void setLastBackupTime(long lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
    }
}
