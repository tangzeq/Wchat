package tangzeqi.com.tools.mind.core;

import tangzeqi.com.tools.mind.storage.MemoryEntry;
import tangzeqi.com.tools.mind.analysis.SemanticAnalyzer;
import tangzeqi.com.tools.mind.analysis.DefaultSemanticAnalyzer;
import tangzeqi.com.tools.mind.logging.Logger;
import tangzeqi.com.tools.mind.logging.DefaultLogger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认搜索引擎实现
 */
public class DefaultSearchEngine implements SearchEngine {
    private final SemanticAnalyzer semanticAnalyzer;
    private final MemoryManager memoryManager;
    private final Logger logger;
    private final Map<String, Map<String, Double>> queryVectorCache;
    private final int maxCacheSize;
    private final Object cacheLock = new Object();
    private Map<String, Integer> documentFrequencyCache;
    private long lastDocumentFrequencyUpdate;
    private static final long DOCUMENT_FREQUENCY_CACHE_TTL = 60000; // 1分钟缓存

    public DefaultSearchEngine(MemoryManager memoryManager) {
        this.semanticAnalyzer = new DefaultSemanticAnalyzer();
        this.memoryManager = memoryManager;
        this.logger = new DefaultLogger(getClass());
        this.maxCacheSize = 1000;
        this.queryVectorCache = new LinkedHashMap<String, Map<String, Double>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Map<String, Double>> eldest) {
                return size() > maxCacheSize;
            }
        };
        this.documentFrequencyCache = new HashMap<>();
        this.lastDocumentFrequencyUpdate = 0;
    }

    public DefaultSearchEngine(MemoryManager memoryManager, SemanticAnalyzer semanticAnalyzer, int maxCacheSize) {
        this.semanticAnalyzer = semanticAnalyzer;
        this.memoryManager = memoryManager;
        this.logger = new DefaultLogger(getClass());
        this.maxCacheSize = maxCacheSize;
        this.queryVectorCache = new LinkedHashMap<String, Map<String, Double>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Map<String, Double>> eldest) {
                return size() > maxCacheSize;
            }
        };
        this.documentFrequencyCache = new HashMap<>();
        this.lastDocumentFrequencyUpdate = 0;
    }

    @Override
    public List<SearchResult> search(String query, String category, int limit, boolean enableFuzzySearch) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 获取所有记忆
            List<MemoryEntry> allMemories = memoryManager.getAllMemories();
            if (allMemories.isEmpty()) {
                return Collections.emptyList();
            }

            // 过滤分类
            if (category != null && !category.trim().isEmpty()) {
                allMemories = allMemories.stream()
                        .filter(entry -> category.equals(entry.getCategory()))
                        .collect(Collectors.toList());
            }

            // 提取查询关键词
            List<String> queryKeywords = semanticAnalyzer.extractKeywords(query, 100);

            // 构建查询向量（使用缓存）
            Map<String, Double> queryVector = getQueryVector(query, queryKeywords);

            // 计算相似度并排序
            List<SearchResult> results = new ArrayList<>();
            for (MemoryEntry entry : allMemories) {
                double similarity = calculateSimilarity(queryVector, entry);
                double relevance = similarity;

                // 如果启用模糊搜索，计算模糊匹配分数
                if (enableFuzzySearch) {
                    double fuzzyScore = calculateFuzzyMatch(query, entry.getContent());
                    relevance = 0.7 * similarity + 0.3 * fuzzyScore;
                }

                // 增加文本包含检查，如果文本包含查询词，提高相关性
                if (entry.getContent().toLowerCase().contains(query.toLowerCase())) {
                    relevance = Math.max(relevance, 0.5);
                }

                // 只添加相似度大于阈值的结果
                if (relevance > 0.05) {
                    results.add(new SearchResult(
                            entry.getId(),
                            entry.getContent(),
                            entry.getCategory(),
                            similarity,
                            relevance
                    ));
                }
            }

            // 按相关性排序
            results.sort((r1, r2) -> Double.compare(r2.getRelevance(), r1.getRelevance()));

            // 限制结果数量
            if (results.size() > limit) {
                results = results.subList(0, limit);
            }

            return results;
        } catch (Exception e) {
            // 记录异常并返回空结果
            logger.error("Failed to perform search: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public double calculateSimilarity(Map<String, Double> queryVector, MemoryEntry memoryEntry) {
        return semanticAnalyzer.calculateSimilarity(queryVector, memoryEntry.getTfidfVector());
    }

    @Override
    public double calculateFuzzyMatch(String query, String memoryContent) {
        return semanticAnalyzer.calculateFuzzyMatch(query, memoryContent);
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return semanticAnalyzer;
    }

    private Map<String, Double> getQueryVector(String query, List<String> queryKeywords) {
        // 检查缓存（线程安全）
        synchronized (cacheLock) {
            if (queryVectorCache.containsKey(query)) {
                return queryVectorCache.get(query);
            }

            // 计算词频
            Map<String, Integer> termFrequency = semanticAnalyzer.calculateTermFrequency(queryKeywords);

            // 构建文档频率映射
            Map<String, Integer> documentFrequency = buildDocumentFrequency();

            // 构建TF-IDF向量
            Map<String, Double> queryVector = semanticAnalyzer.buildTfidfVector(
                    termFrequency, documentFrequency, memoryManager.getMemoryCount()
            );

            // 缓存查询向量
            queryVectorCache.put(query, queryVector);

            return queryVector;
        }
    }

    private Map<String, Integer> buildDocumentFrequency() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        synchronized (cacheLock) {
            if (documentFrequencyCache != null && !documentFrequencyCache.isEmpty() && 
                (currentTime - lastDocumentFrequencyUpdate) < DOCUMENT_FREQUENCY_CACHE_TTL) {
                return documentFrequencyCache;
            }

            // 缓存无效，重新计算
            Map<String, Integer> documentFrequency = new HashMap<>();
            List<MemoryEntry> allMemories = memoryManager.getAllMemories();

            for (MemoryEntry entry : allMemories) {
                Set<String> terms = entry.getTfidfVector().keySet();
                for (String term : terms) {
                    documentFrequency.put(term, documentFrequency.getOrDefault(term, 0) + 1);
                }
            }

            // 更新缓存
            documentFrequencyCache = documentFrequency;
            lastDocumentFrequencyUpdate = currentTime;

            return documentFrequency;
        }
    }

    /**
     * 清除文档频率缓存
     * 当内存数据发生变化时调用
     */
    public void clearDocumentFrequencyCache() {
        synchronized (cacheLock) {
            documentFrequencyCache.clear();
            lastDocumentFrequencyUpdate = 0;
        }
    }

    /**
     * 清除查询向量缓存
     */
    public void clearQueryVectorCache() {
        synchronized (cacheLock) {
            queryVectorCache.clear();
        }
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCaches() {
        synchronized (cacheLock) {
            clearDocumentFrequencyCache();
            clearQueryVectorCache();
        }
    }
}
