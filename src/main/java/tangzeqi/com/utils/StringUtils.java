package tangzeqi.com.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StringUtils {

    // 停用词列表
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这",
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "with", "by", "of", "from", "as", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did"
    ));

    // 句子成分权重映射
    private static final Map<String, Double> SENTENCE_COMPONENT_WEIGHTS = new HashMap<>();
    static {
        SENTENCE_COMPONENT_WEIGHTS.put("object", 1.0);      // 宾语
        SENTENCE_COMPONENT_WEIGHTS.put("subject", 0.9);    // 主语
        SENTENCE_COMPONENT_WEIGHTS.put("attribute", 0.8);  // 定语
        SENTENCE_COMPONENT_WEIGHTS.put("predicate", 0.7);  // 谓语
        SENTENCE_COMPONENT_WEIGHTS.put("adverbial", 0.6);   // 状语
        SENTENCE_COMPONENT_WEIGHTS.put("complement", 0.5); // 补语
    }

    /**
     * 缓存query的关键词提取结果
     */
    private static final Cache<String, List<Keyword>> QUERY_KEYWORDS_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(100, TimeUnit.MILLISECONDS)
            .maximumSize(1)
            .build();

    /**
     * 关键词类
     */
    private static class Keyword {
        String word;
        double weight;
        String sentenceComponent; // 句子成分

        Keyword(String word, double weight, String sentenceComponent) {
            this.word = word;
            this.weight = weight;
            this.sentenceComponent = sentenceComponent;
        }
    }

    /**
     * 从候选内容中找到最匹配query的内容
     * @param query 查询语句
     * @param contents 候选内容列表
     * @return 最匹配的内容，如果没有匹配则返回null
     */
    public static double calculateSimilarity(String query, String contents) {
        if (query == null || query.trim().isEmpty() || contents == null || contents.trim().isEmpty()) {
            return 0;
        }

        // 提取query的关键词
        List<Keyword> queryKeywords = QUERY_KEYWORDS_CACHE.get(query, k -> extractKeywords(k));

        if (queryKeywords.isEmpty()) {
            return 0;
        }

        return calculateMatchScore(queryKeywords, contents);
    }

    /**
     * 从文本中提取关键词
     */
    private static List<Keyword> extractKeywords(String text) {
        List<Keyword> keywords = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return keywords;
        }

        // 判断文本语言类型
        boolean isChinese = containsChineseCharacters(text);

        // 根据语言类型进行分词和句子成分分析
        if (isChinese) {
            return extractChineseKeywords(text);
        } else {
            return extractEnglishKeywords(text);
        }
    }

    /**
     * 提取中文关键词
     */
    private static List<Keyword> extractChineseKeywords(String text) {
        List<Keyword> keywords = new ArrayList<>();

        // 简单的中文分词：按字符分词
        List<String> chars = new ArrayList<>();
        for (char c : text.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                chars.add(String.valueOf(c));
            }
        }

        // 识别句子成分
        for (int i = 0; i < chars.size(); i++) {
            String current = chars.get(i);

            // 跳过停用词
            if (STOP_WORDS.contains(current)) {
                continue;
            }

            // 判断句子成分
            String sentenceComponent = determineChineseSentenceComponent(chars, i);

            // 获取权重
            double weight = SENTENCE_COMPONENT_WEIGHTS.getOrDefault(sentenceComponent, 0.5);

            keywords.add(new Keyword(current, weight, sentenceComponent));
        }

        return keywords;
    }

    /**
     * 确定中文句子成分
     */
    private static String determineChineseSentenceComponent(List<String> chars, int index) {
        if (index < 0 || index >= chars.size()) {
            return "unknown";
        }

        String current = chars.get(index);

        // 检查是否是"的"字短语（定语）
        if (index < chars.size() - 1 && chars.get(index + 1).equals("的")) {
            return "attribute";
        }

        // 检查是否是"地"字短语（状语）
        if (index < chars.size() - 1 && chars.get(index + 1).equals("地")) {
            return "adverbial";
        }

        // 检查是否是"得"字短语（补语）
        if (index > 0 && chars.get(index - 1).equals("得")) {
            return "complement";
        }

        // 检查是否是"的"字前面的词（可能是主语或宾语）
        if (current.equals("的")) {
            // "的"字前面的词可能是定语，但"的"字本身不是关键词
            return "unknown";
        }

        // 检查是否是动词（谓语）
        if (isChineseVerb(current)) {
            return "predicate";
        }

        // 默认为名词（可能是主语或宾语）
        return "noun";
    }

    /**
     * 检查是否是中文动词
     */
    private static boolean isChineseVerb(String word) {
        // 简化的动词判断逻辑
        // 实际项目中可以使用更复杂的NLP库
        return word.endsWith("了") || word.endsWith("着") || word.endsWith("过") ||
                word.endsWith("起来") || word.endsWith("下去") || word.endsWith("上来") ||
                word.endsWith("下来") || word.endsWith("进去") || word.endsWith("出来");
    }

    /**
     * 提取英文关键词
     */
    private static List<Keyword> extractEnglishKeywords(String text) {
        List<Keyword> keywords = new ArrayList<>();

        // 英文按空格和标点分词
        List<String> words = Arrays.asList(text.toLowerCase().split("[\\s\\p{Punct}]+"));

        // 识别句子成分
        for (int i = 0; i < words.size(); i++) {
            String current = words.get(i);

            // 跳过停用词
            if (STOP_WORDS.contains(current)) {
                continue;
            }

            // 判断句子成分
            String sentenceComponent = determineEnglishSentenceComponent(words, i);

            // 获取权重
            double weight = SENTENCE_COMPONENT_WEIGHTS.getOrDefault(sentenceComponent, 0.5);

            keywords.add(new Keyword(current, weight, sentenceComponent));
        }

        return keywords;
    }

    /**
     * 确定英文句子成分
     */
    private static String determineEnglishSentenceComponent(List<String> words, int index) {
        if (index < 0 || index >= words.size()) {
            return "unknown";
        }

        String current = words.get(index);

        // 检查是否是动词（谓语）
        if (isEnglishVerb(current)) {
            return "predicate";
        }

        // 检查是否是形容词（定语）
        if (isEnglishAdjective(current)) {
            return "attribute";
        }

        // 检查是否是副词（状语）
        if (isEnglishAdverb(current)) {
            return "adverbial";
        }

        // 默认为名词（可能是主语或宾语）
        return "noun";
    }

    /**
     * 检查是否是英文动词
     */
    private static boolean isEnglishVerb(String word) {
        // 简化的动词判断逻辑
        return word.endsWith("ing") || word.endsWith("ed") || word.endsWith("s") ||
                word.endsWith("ize") || word.endsWith("ate") || word.endsWith("ify");
    }

    /**
     * 检查是否是英文形容词
     */
    private static boolean isEnglishAdjective(String word) {
        // 简化的形容词判断逻辑
        return word.endsWith("ful") || word.endsWith("ous") || word.endsWith("ive") ||
                word.endsWith("able") || word.endsWith("ible") || word.endsWith("al");
    }

    /**
     * 检查是否是英文副词
     */
    private static boolean isEnglishAdverb(String word) {
        // 简化的副词判断逻辑
        return word.endsWith("ly") || word.endsWith("wise") || word.endsWith("wards");
    }

    /**
     * 计算内容与关键词的匹配得分
     */
    private static double calculateMatchScore(List<Keyword> keywords, String content) {
        if (keywords.isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        double totalWeight = 0.0;

        for (Keyword keyword : keywords) {
            double weight = keyword.weight;
            totalWeight += weight;

            // 精确匹配
            if (content.contains(keyword.word)) {
                totalScore += weight * 1.0;
            }
        }

        return totalWeight > 0 ? totalScore / totalWeight : 0.0;
    }

    /**
     * 分词处理
     */
    private static List<String> tokenizeText(String text) {
        List<String> tokens = new ArrayList<>();
        boolean containsChinese = containsChineseCharacters(text);

        if (containsChinese) {
            // 中文按字符分词
            for (char c : text.toCharArray()) {
                if (!Character.isWhitespace(c)) {
                    tokens.add(String.valueOf(c));
                }
            }
        } else {
            // 英文按空格和标点分词
            tokens = Arrays.asList(text.toLowerCase().split("[\\s\\p{Punct}]+"));
        }

        return tokens;
    }

    /**
     * 检查是否包含中文字符
     */
    private static boolean containsChineseCharacters(String text) {
        return text.chars().anyMatch(c ->
                Character.UnicodeBlock.of((char) c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
        );
    }
}
