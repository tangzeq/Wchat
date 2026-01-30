package tangzeqi.com.tools.mind.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 默认配置管理器实现
 */
public class DefaultConfigurationManager implements ConfigurationManager {
    private Configuration configuration;
    private final ObjectMapper objectMapper;

    public DefaultConfigurationManager() {
        this.configuration = new Configuration();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void loadConfiguration(String configPath) {
        try {
            Path path = Paths.get(configPath);
            if (Files.exists(path)) {
                configuration = objectMapper.readValue(path.toFile(), Configuration.class);
            }
        } catch (IOException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            // 使用默认配置
            configuration = new Configuration();
        }
    }

    @Override
    public void saveConfiguration(String configPath) {
        try {
            Path path = Paths.get(configPath);
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            objectMapper.writeValue(path.toFile(), configuration);
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }

    @Override
    public void updateConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
