package tangzeqi.com.tools.mind.core;

import tangzeqi.com.tools.mind.storage.MemoryEntry;
import tangzeqi.com.tools.mind.storage.DataStore;
import java.util.List;
import java.util.Optional;

/**
 * 内存管理器接口 - 管理记忆条目的生命周期
 */
public interface MemoryManager {
    /**
     * 添加记忆
     * @param content 记忆内容
     * @param category 分类
     * @return 记忆条目ID
     */
    String addMemory(String content, String category);

    /**
     * 获取记忆
     * @param id 记忆ID
     * @return 记忆条目，不存在则返回Optional.empty()
     */
    Optional<MemoryEntry> getMemory(String id);

    /**
     * 根据内容获取记忆
     * @param content 记忆内容
     * @return 记忆条目，不存在则返回Optional.empty()
     */
    Optional<MemoryEntry> getMemoryByContent(String content);

    /**
     * 获取所有记忆
     * @return 记忆条目列表
     */
    List<MemoryEntry> getAllMemories();

    /**
     * 根据分类获取记忆
     * @param category 分类
     * @return 记忆条目列表
     */
    List<MemoryEntry> getMemoriesByCategory(String category);

    /**
     * 更新记忆
     * @param id 记忆ID
     * @param content 新的记忆内容
     * @param category 新的分类
     * @return 更新是否成功
     */
    boolean updateMemory(String id, String content, String category);

    /**
     * 删除记忆
     * @param id 记忆ID
     * @return 删除是否成功
     */
    boolean deleteMemory(String id);

    /**
     * 清空所有记忆
     */
    void clearAllMemories();

    /**
     * 获取记忆数量
     * @return 记忆数量
     */
    int getMemoryCount();

    /**
     * 检查是否为空
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * 检查是否包含指定ID的记忆
     * @param id 记忆ID
     * @return 是否包含
     */
    boolean containsMemory(String id);

    /**
     * 检查是否包含指定内容的记忆
     * @param content 记忆内容
     * @return 是否包含
     */
    boolean containsMemoryContent(String content);

    /**
     * 获取数据存储
     * @return 数据存储
     */
    DataStore getDataStore();
}
