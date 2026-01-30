package tangzeqi.com.tools.mind.config;

/**
 * 配置管理器接口 - 管理系统配置
 */
public interface ConfigurationManager {
    /**
     * 获取配置
     * @return 配置对象
     */
    Configuration getConfiguration();

    /**
     * 加载配置
     * @param configPath 配置文件路径
     */
    void loadConfiguration(String configPath);

    /**
     * 保存配置
     * @param configPath 配置文件路径
     */
    void saveConfiguration(String configPath);

    /**
     * 更新配置
     * @param configuration 新的配置对象
     */
    void updateConfiguration(Configuration configuration);
}
