package tangzeqi.com.tools.mind.storage;

import java.util.List;
import java.util.Optional;

/**
 * 数据存储接口 - 内存数据存储
 */
public interface DataStore {
    /**
     * 添加记忆条目
     * @param entry 记忆条目
     */
    void add(MemoryEntry entry);

    /**
     * 根据ID获取记忆条目
     * @param id 记忆ID
     * @return 记忆条目，不存在则返回Optional.empty()
     */
    Optional<MemoryEntry> get(String id);

    /**
     * 根据内容获取记忆条目
     * @param content 记忆内容
     * @return 记忆条目，不存在则返回Optional.empty()
     */
    Optional<MemoryEntry> getByContent(String content);

    /**
     * 获取所有记忆条目
     * @return 记忆条目列表
     */
    List<MemoryEntry> getAll();

    /**
     * 根据分类获取记忆条目
     * @param category 分类
     * @return 记忆条目列表
     */
    List<MemoryEntry> getByCategory(String category);

    /**
     * 更新记忆条目
     * @param entry 记忆条目
     * @return 更新是否成功
     */
    boolean update(MemoryEntry entry);

    /**
     * 删除记忆条目
     * @param id 记忆ID
     * @return 删除是否成功
     */
    boolean delete(String id);

    /**
     * 清空所有记忆条目
     */
    void clear();

    /**
     * 获取记忆条目数量
     * @return 记忆条目数量
     */
    int size();

    /**
     * 检查是否为空
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * 检查是否包含指定ID的记忆条目
     * @param id 记忆ID
     * @return 是否包含
     */
    boolean contains(String id);

    /**
     * 检查是否包含指定内容的记忆条目
     * @param content 记忆内容
     * @return 是否包含
     */
    boolean containsContent(String content);
}
