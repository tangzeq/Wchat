package tangzeqi.com.tools.mind.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import tangzeqi.com.tools.mind.MindService;
import tangzeqi.com.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public void set(String mind, MindProgressListener listener) {
        // 参数验证
        if (mind == null || mind.trim().isEmpty()) {
            if (listener != null) {
                listener.onComplete(Collections.emptyList(), 0, 0);
            }
            return;
        }

        String trimmedMind = mind.trim();

        try {

            // 开始处理
            if (listener != null) {
                listener.onStart(trimmedMind);
            }

            // 基于文件的去重检查
            AtomicInteger fileCount = new AtomicInteger(0);
            AtomicInteger lineCount = new AtomicInteger(0);
            AtomicBoolean unSet = new AtomicBoolean(false);
            if (Files.exists(STORAGE_DIR) && Files.isDirectory(STORAGE_DIR)) {
                // 使用Files.find代替Files.walk，减少递归开销
                try (Stream<Path> find = Files.find(STORAGE_DIR, 10, (path, attr) -> {
                    if (!attr.isRegularFile()) return false;
                    String name = path.getFileName().toString();
                    return name.endsWith(".txt") || name.endsWith(".json");
                })) {
                    find.parallel().anyMatch(file -> {
                        if (unSet.get()) {
                            return true;
                        }
                        try (Stream<String> lines = Files.lines(file)) {
                            lines.anyMatch(line -> {
                                // 已找到重复，跳过后续处理
                                if (unSet.get()) {
                                    return true;
                                }
                                int currentLine = lineCount.incrementAndGet();
                                if (listener != null) {
                                    listener.onSearchProgress(fileCount.get(), currentLine);
                                }
                                if (line != null && !line.trim().isEmpty()) {
                                    String extractedContent = extractContent(line, file);
                                    if (extractedContent != null && extractedContent.equals(trimmedMind)) {
                                        unSet.set(true);
                                        return true;
                                    }
                                }
                               return false;
                            });
                        } catch (IOException e) {
                            // 处理失败，继续处理其他文件
                        }
                        // 每处理完一个文件更新文件计数
                        fileCount.incrementAndGet();
                        return unSet.get();
                    });
                }
            }
            if(!unSet.get()) {
                // 获取或创建合适的文件
                Path targetFile = getOrCreateFile();
                // 追加写入到文件
                Files.write(targetFile,
                        Collections.singletonList(trimmedMind),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            }
            if (listener != null) {
                listener.onSave(fileCount.get(), lineCount.get(),!unSet.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
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

            // 顺序流式处理，只维护Top-10结果
            // 使用PriorityQueue实现自动排序，Set实现去重
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
                    // 使用Files.find代替Files.walk，减少递归开销
                    try (Stream<Path> find = Files.find(STORAGE_DIR, 10, (path, attr) -> {
                        if (!attr.isRegularFile()) return false;
                        String name = path.getFileName().toString();
                        return name.endsWith(".txt") || name.endsWith(".json");
                    })) {
                        // 并行处理文件，提高性能
                        find.parallel().forEach(file -> {
                            try (Stream<String> lines = Files.lines(file)) {
                                // 行级别并行处理，提高性能
                                lines.parallel().forEach(line -> {
                                    // 快速检查行是否为空
                                    if (line == null || line.trim().isEmpty()) {
                                        return;
                                    }
                                    
                                    int currentLine = processedLines.incrementAndGet();
                                    if (listener != null ) {
                                        listener.onSearchProgress(processedFiles.get(), currentLine);
                                    }
                                    // 提取内容
                                    String content = extractContent(line, file);
                                    if (content == null || content.trim().isEmpty()) {
                                        return;
                                    }
                                    // 直接使用StringUtils计算相似度，让其内部处理所有文本预处理和语义分析
                                    double score = StringUtils.calculateSimilarity(mind, content);
                                    if (score > 0) {
                                        validContents.incrementAndGet();
                                        // 线程安全地更新结果
                                        synchronized (topResults) {
                                            if (!contentSet.contains(content)) {
                                                if (topResults.size() < TOP_K) {
                                                    // 结果集未满，直接添加
                                                    topResults.offer(new ScoredEntry(content, score));
                                                    contentSet.add(content);
                                                } else if (score > topResults.peek().getScore()) {
                                                    // 结果集已满，但新条目分数更高，替换最低分条目
                                                    ScoredEntry removedEntry = topResults.poll();
                                                    contentSet.remove(removedEntry.getContent());
                                                    topResults.offer(new ScoredEntry(content, score));
                                                    contentSet.add(content);
                                                }
                                            }
                                        }
                                    }
                                });
                                // 每处理完一个文件就更新一次进度
                                int currentFileCount = processedFiles.incrementAndGet();
                            } catch (IOException e) {
                                // 处理失败，不增加文件计数，避免错误统计
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
                if (listener != null) {
                    listener.onComplete(new ArrayList<>(), 0, 0);
                }
                return;
            }

            // 转换为排序后的列表
            List<ScoredEntry> list = topResults.stream().sorted((p1,p2)->Double.compare(p2.getScore(),p1.getScore())).toList();
            if (listener != null) {
                listener.onComplete(list, processedFiles.get(), validContents.get());
            }
        } catch (Exception e) {
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
                        e.printStackTrace();
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
        // 快速检查空行
        if (line == null) {
            return null;
        }
        
        // 尝试直接作为内容处理（最常见的情况）
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()) {
            return null;
        }
        
        try {
            // 尝试作为JSON处理（仅在必要时）
            try {
                // 快速检查是否可能是JSON（以{开头）
                if (trimmedLine.startsWith("{")) {
                    Map<?, ?> jsonMap = objectMapper.readValue(trimmedLine, Map.class);
                    // 尝试多个可能的字段名
                    String[] contentFields = {"content", "text", "message", "data", "value", "contentText"};
                    for (String field : contentFields) {
                        if (jsonMap.containsKey(field)) {
                            Object contentObj = jsonMap.get(field);
                            if (contentObj != null) {
                                String content = contentObj.toString().trim();
                                if (!content.isEmpty()) {
                                    return content;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，继续尝试其他格式
            }

            // 尝试作为文本处理（多种分隔符）
            String[] separators = {"|", ",", ";", "\t", "="};
            for (String separator : separators) {
                int separatorIndex = trimmedLine.indexOf(separator);
                if (separatorIndex != -1 && separatorIndex < trimmedLine.length() - 1) {
                    String content = trimmedLine.substring(separatorIndex + 1).trim();
                    if (!content.isEmpty()) {
                        return content;
                    }
                }
            }
        } catch (Exception e) {
            // 提取失败，忽略此行
        }
        
        // 直接返回处理后的内容
        return trimmedLine;
    }

}
