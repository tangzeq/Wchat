package tangzeqi.com.tools.mind.storage;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 内存数据存储实现 - 使用LinkedHashMap和读写锁
 */
public class InMemoryDataStore implements DataStore {
    private final Map<String, MemoryEntry> idMap;
    private final Map<String, MemoryEntry> contentMap;
    private final Map<String, List<MemoryEntry>> categoryMap;
    private final ReentrantReadWriteLock lock;

    public InMemoryDataStore() {
        this.idMap = new LinkedHashMap<>(16, 0.75f, true);
        this.contentMap = new LinkedHashMap<>(16, 0.75f, true);
        this.categoryMap = new LinkedHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void add(MemoryEntry entry) {
        lock.writeLock().lock();
        try {
            idMap.put(entry.getId(), entry);
            contentMap.put(entry.getContent(), entry);
            categoryMap.computeIfAbsent(entry.getCategory(), k -> new ArrayList<>()).add(entry);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<MemoryEntry> get(String id) {
        lock.readLock().lock();
        try {
            MemoryEntry entry = idMap.get(id);
            if (entry != null) {
                // 注意：这里在读取锁下修改了entry的状态
                // 但这是安全的，因为每个entry是唯一的，且修改的是实例变量
                entry.updateAccessTime();
            }
            return Optional.ofNullable(entry);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<MemoryEntry> getByContent(String content) {
        lock.readLock().lock();
        try {
            MemoryEntry entry = contentMap.get(content);
            if (entry != null) {
                entry.updateAccessTime();
            }
            return Optional.ofNullable(entry);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<MemoryEntry> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(idMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<MemoryEntry> getByCategory(String category) {
        lock.readLock().lock();
        try {
            List<MemoryEntry> entries = categoryMap.get(category);
            if (entries != null) {
                // 这里不需要更新访问时间，因为getByCategory通常用于批量操作
                return new ArrayList<>(entries);
            }
            return Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean update(MemoryEntry entry) {
        lock.writeLock().lock();
        try {
            if (!idMap.containsKey(entry.getId())) {
                return false;
            }

            // 更新idMap
            idMap.put(entry.getId(), entry);

            // 更新contentMap
            contentMap.put(entry.getContent(), entry);

            // 更新categoryMap
            // 先从旧分类中移除
            for (Map.Entry<String, List<MemoryEntry>> entry1 : categoryMap.entrySet()) {
                entry1.getValue().removeIf(e -> e.getId().equals(entry.getId()));
            }
            // 再添加到新分类
            categoryMap.computeIfAbsent(entry.getCategory(), k -> new ArrayList<>()).add(entry);

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean delete(String id) {
        lock.writeLock().lock();
        try {
            MemoryEntry entry = idMap.remove(id);
            if (entry == null) {
                return false;
            }

            // 从contentMap中移除
            contentMap.remove(entry.getContent());

            // 从categoryMap中移除
            List<MemoryEntry> categoryEntries = categoryMap.get(entry.getCategory());
            if (categoryEntries != null) {
                categoryEntries.removeIf(e -> e.getId().equals(id));
                if (categoryEntries.isEmpty()) {
                    categoryMap.remove(entry.getCategory());
                }
            }

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            idMap.clear();
            contentMap.clear();
            categoryMap.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return idMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return idMap.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(String id) {
        lock.readLock().lock();
        try {
            return idMap.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsContent(String content) {
        lock.readLock().lock();
        try {
            return contentMap.containsKey(content);
        } finally {
            lock.readLock().unlock();
        }
    }
}
