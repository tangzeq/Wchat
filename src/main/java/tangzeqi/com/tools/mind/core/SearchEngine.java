package tangzeqi.com.tools.mind.core;

import tangzeqi.com.tools.mind.storage.MemoryEntry;
import tangzeqi.com.tools.mind.analysis.SemanticAnalyzer;
import java.util.List;

/**
 * 搜索引擎接口 - 实现搜索算法和逻辑
 */
public interface SearchEngine {
    /**
     * 搜索记忆
     * @param query 查询文本
     * @param category 分类（可选，null表示不限制分类）
     * @param limit 结果限制
     * @param enableFuzzySearch 是否启用模糊搜索
     * @return 搜索结果列表
     */
    List<SearchResult> search(String query, String category, int limit, boolean enableFuzzySearch);

    /**
     * 计算相似度
     * @param queryVector 查询向量
     * @param memoryEntry 记忆条目
     * @return 相似度分数
     */
    double calculateSimilarity(java.util.Map<String, Double> queryVector, MemoryEntry memoryEntry);

    /**
     * 计算模糊匹配分数
     * @param query 查询文本
     * @param memoryContent 记忆内容
     * @return 模糊匹配分数
     */
    double calculateFuzzyMatch(String query, String memoryContent);

    /**
     * 获取语义分析器
     * @return 语义分析器
     */
    SemanticAnalyzer getSemanticAnalyzer();
}
