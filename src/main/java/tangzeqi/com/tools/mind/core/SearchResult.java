package tangzeqi.com.tools.mind.core;

/**
 * 搜索结果 - 搜索返回的数据结构
 */
public class SearchResult {
    private String id;
    private String content;
    private String category;
    private double score;
    private double relevance;

    public SearchResult(String id, String content, String category, double score, double relevance) {
        this.id = id;
        this.content = content;
        this.category = category;
        this.score = score;
        this.relevance = relevance;
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

    public double getScore() {
        return score;
    }

    public double getRelevance() {
        return relevance;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "id='" + id + "', " +
                "content='" + (content.length() > 50 ? content.substring(0, 50) + "..." : content) + "', " +
                "category='" + category + "', " +
                "score=" + score + ", " +
                "relevance=" + relevance + "" +
                '}';
    }
}
