package tangzeqi.com.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringUtils {

    /**
     * 计算相似度 - 多维度评分，提高准确度
     */
    public static double calculateSimilarity(String query, String content) {
        double score = 0.0;
        String lowerQuery = query.toLowerCase();
        String lowerContent = content.toLowerCase();

        // 1. 完全匹配检查：如果内容与查询完全相同，直接返回1.0
        if (lowerContent.equals(lowerQuery)) {
            return 1.0;
        }

        // 2. 文本包含检查：如果内容包含查询文本，加0.2分
        if (lowerContent.contains(lowerQuery)) {
            score += 0.2;
        }

        // 3. 双向包含检查：如果查询包含内容文本，加0.2分（解决部分匹配问题）
        if (lowerQuery.contains(lowerContent)) {
            score += 0.2;
        }

        // 4. 字符级相似度：计算查询中的每个字符是否在内容中出现
        // 这是最重要的部分，确保即使字符在内容中是分散的，也能获得较高的分数
        int matchingChars = 0;
        for (char c : lowerQuery.toCharArray()) {
            if (lowerContent.indexOf(c) != -1) {
                matchingChars++;
            }
        }
        double charMatchRatio = (double) matchingChars / lowerQuery.length();
        score += 0.5 * charMatchRatio;

        // 5. 词汇匹配：检查查询中的完整单词是否在内容中出现
        String[] queryWords = lowerQuery.split("\\s+");
        String[] contentWords = lowerContent.split("\\s+");
        
        int queryWordCount = queryWords.length;
        int contentWordCount = contentWords.length;
        
        // 计算查询单词在内容中的匹配数量
        int matchedQueryWords = 0;
        for (String word : queryWords) {
            if (lowerContent.contains(word)) {
                matchedQueryWords++;
            }
        }
        
        // 计算内容单词在查询中的匹配数量
        int matchedContentWords = 0;
        for (String word : contentWords) {
            if (lowerQuery.contains(word)) {
                matchedContentWords++;
            }
        }
        
        // 计算词汇匹配分数
        if (queryWordCount > 0) {
            double queryWordMatchRatio = (double) matchedQueryWords / queryWordCount;
            score += 0.2 * queryWordMatchRatio;
        }
        
        if (contentWordCount > 0) {
            double contentWordMatchRatio = (double) matchedContentWords / contentWordCount;
            score += 0.1 * contentWordMatchRatio;
        }

        // 6. 核心词汇匹配：检查查询中的核心词汇是否在内容中出现
        boolean hasCoreMatch = false;
        for (String queryWord : queryWords) {
            for (String contentWord : contentWords) {
                if (hasPartialMatch(queryWord, contentWord) || hasPartialMatch(contentWord, queryWord)) {
                    hasCoreMatch = true;
                    break;
                }
            }
            if (hasCoreMatch) {
                break;
            }
        }
        if (hasCoreMatch) {
            score += 0.1 * charMatchRatio;
        }

        // 7. 阈值过滤：如果总分低于0.1，视为不相关，返回0.0
        if (score < 0.1) {
            return 0.0;
        }

        // 8. 结果归一化：确保最终得分不超过1.0
        return Math.min(score, 1.0);
    }
    
    /**
     * 检查部分匹配：如果word的大部分字符在target中出现，则认为是部分匹配
     */
    private static boolean hasPartialMatch(String word, String target) {
        if (word.length() <= 1) {
            return false;
        }
        
        int matchingChars = 0;
        for (char c : word.toCharArray()) {
            if (target.indexOf(c) != -1) {
                matchingChars++;
            }
        }
        
        // 如果匹配字符数超过单词长度的60%，则认为是部分匹配
        return (double) matchingChars / word.length() > 0.6;
    }
    
    /**
     * 计算两个字符串的最长公共子序列长度
     */
    private static int longestCommonSubsequence(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }

}
