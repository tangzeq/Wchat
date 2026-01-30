package tangzeqi.com.tools.mind.analysis;

import java.util.List;
import java.util.Map;

/**
 * 语义分析器接口 - 用于关键词提取和向量生成
 */
public interface SemanticAnalyzer {
    /**
     * 提取关键词
     * @param text 文本内容
     * @param maxKeywords 最大关键词数量
     * @return 关键词列表
     */
    List<String> extractKeywords(String text, int maxKeywords);

    /**
     * 计算词频
     * @param keywords 关键词列表
     * @return 词频映射
     */
    Map<String, Integer> calculateTermFrequency(List<String> keywords);

    /**
     * 构建TF-IDF向量
     * @param termFrequency 词频映射
     * @param documentFrequency 文档频率映射
     * @param totalDocuments 总文档数
     * @return TF-IDF向量
     */
    Map<String, Double> buildTfidfVector(Map<String, Integer> termFrequency,
                                         Map<String, Integer> documentFrequency,
                                         int totalDocuments);

    /**
     * 计算文档频率
     * @param entries 记忆条目列表
     * @return 文档频率映射
     */
    Map<String, Integer> calculateDocumentFrequency(List<String> entries);

    /**
     * 计算相似度
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 相似度分数
     */
    double calculateSimilarity(Map<String, Double> vector1, Map<String, Double> vector2);

    /**
     * 计算模糊匹配分数
     * @param text1 文本1
     * @param text2 文本2
     * @return 模糊匹配分数
     */
    double calculateFuzzyMatch(String text1, String text2);
}
