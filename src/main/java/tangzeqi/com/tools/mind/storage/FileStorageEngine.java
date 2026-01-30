package tangzeqi.com.tools.mind.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储引擎实现 - 使用Java NIO
 */
public class FileStorageEngine implements StorageEngine {

    @Override
    public List<String> read(Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    @Override
    public void write(Path path, List<String> content) throws IOException {
        createDirectories(path.getParent());
        Files.write(path, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    @Override
    public void writeAtomic(Path path, List<String> content) throws IOException {
        createDirectories(path.getParent());
        Path tempPath = path.resolveSibling(path.getFileName() + ".tmp");
        Files.write(tempPath, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public void createDirectory(Path path) throws IOException {
        Files.createDirectory(path);
    }

    @Override
    public void createDirectories(Path path) throws IOException {
        if (path != null) {
            Files.createDirectories(path);
        }
    }

    @Override
    public boolean delete(Path path) throws IOException {
        return Files.deleteIfExists(path);
    }

    @Override
    public List<Path> list(Path path) throws IOException {
        List<Path> paths = new ArrayList<>();
        if (Files.exists(path) && Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    paths.add(entry);
                }
            }
        }
        return paths;
    }

    @Override
    public List<Path> list(Path path, String extension) throws IOException {
        List<Path> paths = new ArrayList<>();
        if (Files.exists(path) && Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, extension)) {
                for (Path entry : stream) {
                    paths.add(entry);
                }
            }
        }
        return paths;
    }

    @Override
    public long size(Path path) throws IOException {
        return Files.size(path);
    }

    @Override
    public long lastModified(Path path) throws IOException {
        return Files.getLastModifiedTime(path).toMillis();
    }

    @Override
    public void move(Path source, Path target) throws IOException {
        createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void copy(Path source, Path target) throws IOException {
        createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
