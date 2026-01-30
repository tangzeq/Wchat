package tangzeqi.com.tools.mind.core;

import tangzeqi.com.tools.mind.storage.StorageEngine;
import tangzeqi.com.tools.mind.storage.MemoryEntry;
import tangzeqi.com.tools.mind.exception.PersistenceException;
import java.nio.file.Path;
import java.util.List;

/**
 * 持久化管理器接口 - 管理数据持久化策略
 */
public interface PersistenceManager {
    /**
     * 保存数据到存储
     * @param entries 记忆条目列表
     * @throws PersistenceException 持久化异常
     */
    void save(List<MemoryEntry> entries) throws PersistenceException;

    /**
     * 从存储加载数据
     * @return 记忆条目列表
     * @throws PersistenceException 持久化异常
     */
    List<MemoryEntry> load() throws PersistenceException;

    /**
     * 执行备份
     * @throws PersistenceException 持久化异常
     */
    void backup() throws PersistenceException;

    /**
     * 从备份恢复
     * @return 恢复是否成功
     * @throws PersistenceException 持久化异常
     */
    boolean recoverFromBackup() throws PersistenceException;

    /**
     * 清理旧备份
     * @throws PersistenceException 持久化异常
     */
    void cleanupOldBackups() throws PersistenceException;

    /**
     * 获取存储路径
     * @return 存储路径
     */
    Path getStoragePath();

    /**
     * 获取备份路径
     * @return 备份路径
     */
    Path getBackupPath();

    /**
     * 设置存储路径
     * @param storagePath 存储路径
     */
    void setStoragePath(Path storagePath);

    /**
     * 设置备份路径
     * @param backupPath 备份路径
     */
    void setBackupPath(Path backupPath);

    /**
     * 获取存储引擎
     * @return 存储引擎
     */
    StorageEngine getStorageEngine();

    /**
     * 设置存储引擎
     * @param storageEngine 存储引擎
     */
    void setStorageEngine(StorageEngine storageEngine);

    /**
     * 获取最大备份文件数
     * @return 最大备份文件数
     */
    int getMaxBackups();

    /**
     * 设置最大备份文件数
     * @param maxBackups 最大备份文件数
     */
    void setMaxBackups(int maxBackups);

    /**
     * 获取备份间隔（小时）
     * @return 备份间隔（小时）
     */
    int getBackupIntervalHours();

    /**
     * 设置备份间隔（小时）
     * @param backupIntervalHours 备份间隔（小时）
     */
    void setBackupIntervalHours(int backupIntervalHours);

    /**
     * 获取上次备份时间戳
     * @return 上次备份时间戳
     */
    long getLastBackupTime();

    /**
     * 设置上次备份时间戳
     * @param lastBackupTime 上次备份时间戳
     */
    void setLastBackupTime(long lastBackupTime);
}
