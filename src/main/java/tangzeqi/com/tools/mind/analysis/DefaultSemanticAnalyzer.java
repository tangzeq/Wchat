package tangzeqi.com.tools.mind.analysis;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认语义分析器实现
 */
public class DefaultSemanticAnalyzer implements SemanticAnalyzer {
    private final Map<String, Integer> documentFrequency;
    private int totalDocuments;

    public DefaultSemanticAnalyzer() {
        this.documentFrequency = new HashMap<>();
        this.totalDocuments = 0;
    }

    @Override
    public List<String> extractKeywords(String text, int maxKeywords) {
        // 简单的关键词提取实现
        // 实际项目中可以使用更复杂的NLP库
        List<String> words = tokenize(text);
        Map<String, Integer> wordCount = countWords(words);
        
        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(maxKeywords)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> calculateTermFrequency(List<String> keywords) {
        return countWords(keywords);
    }

    @Override
    public Map<String, Double> buildTfidfVector(Map<String, Integer> termFrequency,
                                               Map<String, Integer> documentFrequency,
                                               int totalDocuments) {
        Map<String, Double> tfidfVector = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();
            int df = documentFrequency.getOrDefault(term, 0);
            
            // 计算TF-IDF
            double idf = Math.log((double) totalDocuments / (df + 1)) + 1.0;
            double tfidf = tf * idf;
            
            tfidfVector.put(term, tfidf);
        }
        
        return tfidfVector;
    }

    @Override
    public Map<String, Integer> calculateDocumentFrequency(List<String> entries) {
        Map<String, Integer> dfMap = new HashMap<>();
        
        for (String entry : entries) {
            List<String> keywords = extractKeywords(entry, 100);
            Set<String> uniqueKeywords = new HashSet<>(keywords);
            
            for (String keyword : uniqueKeywords) {
                dfMap.put(keyword, dfMap.getOrDefault(keyword, 0) + 1);
            }
        }
        
        return dfMap;
    }

    @Override
    public double calculateSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> allTerms = new HashSet<>(vector1.keySet());
        allTerms.addAll(vector2.keySet());
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String term : allTerms) {
            double val1 = vector1.getOrDefault(term, 0.0);
            double val2 = vector2.getOrDefault(term, 0.0);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }
        
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    public double calculateFuzzyMatch(String text1, String text2) {
        // 使用Levenshtein距离计算模糊匹配分数
        int distance = levenshteinDistance(text1.toLowerCase(), text2.toLowerCase());
        int maxLength = Math.max(text1.length(), text2.length());
        return maxLength > 0 ? 1.0 - (double) distance / maxLength : 1.0;
    }

    private List<String> tokenize(String text) {
        // 改进的分词实现，支持中文
        // 实际项目中可以使用更复杂的NLP库如HanLP
        List<String> tokens = new ArrayList<>();
        
        // 处理中文
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                // 英文和数字
                StringBuilder sb = new StringBuilder();
                while (i < text.length() && Character.isLetterOrDigit(text.charAt(i))) {
                    sb.append(text.charAt(i));
                    i++;
                }
                i--;
                String word = sb.toString().toLowerCase();
                if (word.length() > 1) {
                    tokens.add(word);
                }
            } else if (isChineseCharacter(c)) {
                // 中文字符
                tokens.add(String.valueOf(c));
            }
        }
        
        return tokens;
    }
    
    /**
     * 判断是否为中文字符
     * @param c 字符
     * @return 是否为中文字符
     */
    private boolean isChineseCharacter(char c) {
        return Character.UnicodeBlock.of(c).equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) ||
               Character.UnicodeBlock.of(c).equals(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) ||
               Character.UnicodeBlock.of(c).equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
    }

    private Map<String, Integer> countWords(List<String> words) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }
        return wordCount;
    }

    private int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }
}
