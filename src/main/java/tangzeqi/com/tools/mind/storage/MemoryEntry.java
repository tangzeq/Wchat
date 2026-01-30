package tangzeqi.com.tools.mind.storage;

import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 记忆条目 - 核心数据模型
 */
public class MemoryEntry {
    private final String id;
    private final String content;
    private final String category;
    private final Map<String, Double> tfidfVector;
    private final long createdAt;
    private long lastAccessedAt;
    private int accessCount;

    public MemoryEntry(String content, String category, Map<String, Double> tfidfVector) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.category = category;
        this.tfidfVector = tfidfVector;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = System.currentTimeMillis();
        this.accessCount = 0;
    }

    @JsonCreator
    public MemoryEntry(@JsonProperty("id") String id,
                      @JsonProperty("content") String content,
                      @JsonProperty("category") String category,
                      @JsonProperty("tfidfVector") Map<String, Double> tfidfVector,
                      @JsonProperty("createdAt") long createdAt,
                      @JsonProperty("lastAccessedAt") long lastAccessedAt,
                      @JsonProperty("accessCount") int accessCount) {
        this.id = id;
        this.content = content;
        this.category = category;
        this.tfidfVector = tfidfVector;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
        this.accessCount = accessCount;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public Map<String, Double> getTfidfVector() {
        return tfidfVector;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastAccessedAt() {
        return lastAccessedAt;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void updateAccessTime() {
        this.lastAccessedAt = System.currentTimeMillis();
        this.accessCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryEntry that = (MemoryEntry) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "MemoryEntry{" +
                "id='" + id + "', " +
                "content='" + (content.length() > 50 ? content.substring(0, 50) + "..." : content) + "', " +
                "category='" + category + "', " +
                "createdAt=" + createdAt + ", " +
                "lastAccessedAt=" + lastAccessedAt + ", " +
                "accessCount=" + accessCount + "" +
                '}';
    }
}
