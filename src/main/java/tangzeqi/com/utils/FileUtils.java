package tangzeqi.com.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FileUtils {
    /**
     * 安全地执行文件操作
     *
     * @param filePath  文件路径
     * @param operation 文件操作函数
     * @return 操作结果
     */
    public static List<String> safeFileOperation(Path filePath, Function<List<String>, List<String>> operation) {
        try (FileChannel channel = FileChannel.open(filePath,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            List<String> list = readLines(channel);
            list = operation.apply(list);
            writeLines(channel, list);
            return list;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to perform file operation: " + filePath, e);
        }
    }

    /**
     * 使用 FileChannel 读取文件内容
     */
    private static List<String> readLines(FileChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        StringBuilder sb = new StringBuilder();
        List<String> lines = new ArrayList<>();

        long position = channel.position();
        channel.position(0);

        while (channel.read(buffer) != -1) {
            buffer.flip();
            sb.append(StandardCharsets.UTF_8.decode(buffer));
            buffer.clear();
        }

        channel.position(position);

        // 分割行
        String content = sb.toString();
        int start = 0;
        int end;
        while ((end = content.indexOf('\n', start)) != -1) {
            lines.add(content.substring(start, end));
            start = end + 1;
        }
        if (start < content.length()) {
            lines.add(content.substring(start));
        }

        return lines;
    }

    /**
     * 使用 FileChannel 写入文件内容
     */
    private static void writeLines(FileChannel channel, List<String> lines) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append('\n');
        }

        ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8));
        channel.position(0);
        channel.write(buffer);
        channel.truncate(channel.position());
    }

}
