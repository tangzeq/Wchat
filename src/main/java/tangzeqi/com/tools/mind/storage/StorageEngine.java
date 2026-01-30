package tangzeqi.com.tools.mind.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 存储引擎接口 - 负责文件存储和读写操作
 */
public interface StorageEngine {
    /**
     * 读取文件内容
     * @param path 文件路径
     * @return 文件内容列表
     * @throws IOException IO异常
     */
    List<String> read(Path path) throws IOException;

    /**
     * 写入文件内容
     * @param path 文件路径
     * @param content 文件内容列表
     * @throws IOException IO异常
     */
    void write(Path path, List<String> content) throws IOException;

    /**
     * 使用原子操作写入文件
     * @param path 文件路径
     * @param content 文件内容列表
     * @throws IOException IO异常
     */
    void writeAtomic(Path path, List<String> content) throws IOException;

    /**
     * 检查文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    boolean exists(Path path);

    /**
     * 创建目录
     * @param path 目录路径
     * @throws IOException IO异常
     */
    void createDirectory(Path path) throws IOException;

    /**
     * 创建目录（包括父目录）
     * @param path 目录路径
     * @throws IOException IO异常
     */
    void createDirectories(Path path) throws IOException;

    /**
     * 删除文件
     * @param path 文件路径
     * @return 删除是否成功
     * @throws IOException IO异常
     */
    boolean delete(Path path) throws IOException;

    /**
     * 列出目录内容
     * @param path 目录路径
     * @return 文件路径列表
     * @throws IOException IO异常
     */
    List<Path> list(Path path) throws IOException;

    /**
     * 列出目录中指定扩展名的文件
     * @param path 目录路径
     * @param extension 文件扩展名（例如 "*.json"）
     * @return 文件路径列表
     * @throws IOException IO异常
     */
    List<Path> list(Path path, String extension) throws IOException;

    /**
     * 获取文件大小
     * @param path 文件路径
     * @return 文件大小（字节）
     * @throws IOException IO异常
     */
    long size(Path path) throws IOException;

    /**
     * 获取文件最后修改时间
     * @param path 文件路径
     * @return 最后修改时间（毫秒）
     * @throws IOException IO异常
     */
    long lastModified(Path path) throws IOException;

    /**
     * 移动文件
     * @param source 源文件路径
     * @param target 目标文件路径
     * @throws IOException IO异常
     */
    void move(Path source, Path target) throws IOException;

    /**
     * 复制文件
     * @param source 源文件路径
     * @param target 目标文件路径
     * @throws IOException IO异常
     */
    void copy(Path source, Path target) throws IOException;
}
