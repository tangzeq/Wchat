package tangzeqi.com.tools.mind;

import com.fasterxml.jackson.databind.ObjectMapper;
import tangzeqi.com.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 轻量级记忆服务实现 - 基于文件流处理
 * 核心特性：绝对跨项目、跨平台、实时、高准确度、高效、支持海量记忆、用户自由操作
 * 技术原则：无缓存、无定期操作、无热点数据、完全流式处理
 */
public class LightweightMindService implements MindService {
    private static final Path STORAGE_DIR = Paths.get(System.getProperty("user.home"), ".mind-idea-plugin");
    private static final int TOP_K = 10; // 只维护Top-10结果
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 单个文件最大100MB
    private final ObjectMapper objectMapper; // 用于解析JSON格式

    public LightweightMindService() {
        // 确保目录存在
        try {
            Files.createDirectories(STORAGE_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 初始化ObjectMapper用于解析JSON
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String set(String mind) {
        if (mind == null || mind.trim().isEmpty()) {
            return null;
        }

        String trimmedMind = mind.trim();

        // 基于文件的去重检查（支持多格式）
        if (isDuplicate(trimmedMind)) {
            return "DUPLICATE";
        }

        try {
            // 获取或创建合适的文件
            Path targetFile = getOrCreateFile();

            // 追加写入到文件
            String entry = System.currentTimeMillis() + "|" + trimmedMind;
            Files.write(targetFile,
                    Collections.singletonList(entry),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            return entry;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void get(String mind, MindProgressListener listener) {
        if (mind == null || mind.trim().isEmpty()) {
            if (listener != null) {
                listener.onComplete(new ArrayList<>(), 0, 0);
            }
            return;
        }

        try {

            // 开始查找
            if (listener != null) {
                listener.onStart(mind);
            }

            // 并行流式处理，只维护Top-10结果
            PriorityQueue<ScoredEntry> topResults = new PriorityQueue<>(TOP_K,
                    Comparator.comparingDouble(ScoredEntry::getScore));

            // 用于去重的内容集合，只存储Top-K结果的内容
            Set<String> contentSet = new HashSet<>();

            // 处理文件的计数器
            AtomicInteger processedFiles = new AtomicInteger(0);
            AtomicInteger processedLines = new AtomicInteger(0);
            AtomicInteger validContents = new AtomicInteger(0);

            // 初始化完成，开始查找
            try {
                if (Files.exists(STORAGE_DIR) && Files.isDirectory(STORAGE_DIR)) {
                    // 单次遍历：直接并行处理文件内容，不计算总文件数
                    try (Stream<Path> walk = Files.walk(STORAGE_DIR, 10)) { // 递归深度10
                        // 直接并行处理，不收集到列表，只处理.txt和.json文件
                        walk.filter(path -> {
                            String name = path.getFileName().toString();
                            return (name.endsWith(".txt") || name.endsWith(".json")) &&
                                   Files.isRegularFile(path);
                        }).parallel().forEach(file -> {
                            try (Stream<String> lines = Files.lines(file)) {
                                // 直接处理流，不设置行处理上限
                                lines.parallel().forEach(line -> {
                                    processedLines.incrementAndGet();
                                    // 直接更新进度，时间间隔由监听器实现控制
                                    // 由于不知道总文件数，只显示已处理的文件数
                                    if (listener != null) {
                                        listener.onSearchProgress(processedFiles.get(), processedLines.get());
                                    }
                                    if (line != null && !line.trim().isEmpty()) {
                                        String content = extractContent(line, file);
                                        if (content != null) {
                                            validContents.incrementAndGet();
                                            double score = StringUtils.calculateSimilarity(mind, content);
                                            if (score > 0) {
                                                // 检查内容是否已存在于结果中
                                                synchronized (contentSet) {
                                                    if (!contentSet.contains(content)) {
                                                        addToTopResults(topResults, contentSet, new ScoredEntry(content, score));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });

                                // 每处理完一个文件就更新一次进度
                                int currentFileCount = processedFiles.incrementAndGet();
                                if (listener != null) {
                                    listener.onSearchProgress(currentFileCount, processedLines.get());
                                }
                            } catch (IOException e) {
                                // 处理失败，不增加文件计数，避免错误统计
                                if (listener != null) {
                                    listener.onSearchProgress(processedFiles.get(), processedLines.get());
                                }
                            }
                        });
                    }
                } else {
                    if (listener != null) {
                        listener.onComplete(new ArrayList<>(), 0, 0);
                    }
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onComplete(new ArrayList<>(), 0, 0);
                }
                return;
            }


            // 转换为排序后的列表
            List<String> results = new ArrayList<>();
            while (!topResults.isEmpty()) {
                ScoredEntry entry = topResults.poll();
                results.add(entry.getContent());
            }
            // 反转列表，使分数高的在前
            Collections.reverse(results);

            for (int i = 0; i < results.size(); i++) {
                String result = results.get(i);
            }

            if (listener != null) {
                listener.onComplete(results, processedFiles.get(), validContents.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onComplete(new ArrayList<>(), 0, 0);
            }
        }
    }

    /**
     * 获取或创建合适的文件
     */
    private Path getOrCreateFile() throws IOException {
        if (Files.exists(STORAGE_DIR) && Files.isDirectory(STORAGE_DIR)) {
            // 直接使用Files.walk流式处理，查找大小合适的文件
            try (Stream<Path> walk = Files.walk(STORAGE_DIR, 10)) { // 递归深度10
                Optional<Path> suitableFile = walk.filter(path -> {
                    String name = path.getFileName().toString();
                    return (name.endsWith(".txt") || name.endsWith(".json")) &&
                            Files.isRegularFile(path);
                }).filter(file -> {
                    try {
                        return Files.size(file) < MAX_FILE_SIZE;
                    } catch (IOException e) {
                        return false;
                    }
                }).findFirst();

                if (suitableFile.isPresent()) {
                    return suitableFile.get();
                }
            }
        }

        // 创建新文件
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Path newFile = STORAGE_DIR.resolve("memory_" + timestamp + ".txt");
        Files.createFile(newFile);
        return newFile;
    }

    /**
     * 提取文件内容（支持多格式）
     */
    private String extractContent(String line, Path file) {
        try {
            // 尝试作为JSON处理
            try {
                Map<?, ?> jsonMap = objectMapper.readValue(line, Map.class);
                // 尝试多个可能的字段名
                String[] contentFields = {"content", "text", "message", "data", "value", "contentText"};
                for (String field : contentFields) {
                    if (jsonMap.containsKey(field)) {
                        Object contentObj = jsonMap.get(field);
                        String content = contentObj != null ? contentObj.toString().trim() : null;
                        if (content != null && !content.isEmpty()) {
                            return content;
                        }
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，尝试作为文本处理
            }

            // 尝试作为文本处理（多种分隔符）
            String[] separators = {"|", ",", ";", "\t", "="};
            for (String separator : separators) {
                String[] parts = line.split(separator, 2);
                if (parts.length == 2) {
                    String content = parts[1].trim();
                    if (!content.isEmpty()) {
                        return content;
                    }
                }
            }

            // 尝试直接作为内容处理
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                return trimmedLine;
            }
        } catch (Exception e) {
            // 提取失败，忽略此行
        }
        return null;
    }

    /**
     * 基于文件的去重检查（支持多格式）
     */
    private boolean isDuplicate(String content) {
        try {
            if (Files.exists(STORAGE_DIR) && Files.isDirectory(STORAGE_DIR)) {
                // 直接使用Files.walk流式处理，不收集到列表
                try (Stream<Path> walk = Files.walk(STORAGE_DIR, 10)) { // 递归深度10
                    // 并行去重检查
                    return walk.filter(path -> {
                        String name = path.getFileName().toString();
                        return (name.endsWith(".txt") || name.endsWith(".json")) &&
                                Files.isRegularFile(path);
                    }).parallel().anyMatch(file -> {
                        try (Stream<String> lines = Files.lines(file)) {
                            return lines.anyMatch(line -> {
                                if (line != null && !line.trim().isEmpty()) {
                                    String extractedContent = extractContent(line, file);
                                    return extractedContent != null && extractedContent.equals(content);
                                }
                                return false;
                            });
                        } catch (IOException e) {
                            return false;
                        }
                    });
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 添加到Top-K结果
     */
    private void addToTopResults(PriorityQueue<ScoredEntry> topResults, Set<String> contentSet, ScoredEntry entry) {
        if (topResults.size() < TOP_K) {
            topResults.offer(entry);
            contentSet.add(entry.getContent());
        } else if (entry.getScore() > topResults.peek().getScore()) {
            ScoredEntry removedEntry = topResults.poll();
            contentSet.remove(removedEntry.getContent());
            topResults.offer(entry);
            contentSet.add(entry.getContent());
        }
    }


    /**
     * 带分数的条目
     */
    private static class ScoredEntry {
        private final String content;
        private final double score;

        ScoredEntry(String content, double score) {
            this.content = content;
            this.score = score;
        }

        public String getContent() {
            return content;
        }

        public double getScore() {
            return score;
        }
    }
}
