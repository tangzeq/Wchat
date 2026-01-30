package tangzeqi.com.tools.mind.config;

/**
 * 配置类 - 管理系统配置
 */
public class Configuration {
    private String storagePath;
    private String backupPath;
    private int maxCacheSize;
    private int backupIntervalHours;
    private int maxBackups;
    private int searchResultLimit;
    private double searchSimilarityThreshold;
    private double fuzzyMatchThreshold;

    public Configuration() {
        // 默认配置
        this.storagePath = System.getProperty("user.home") + "/.mind-idea-plugin/memory.json";
        this.backupPath = System.getProperty("user.home") + "/.mind-idea-plugin/backups";
        this.maxCacheSize = 1000;
        this.backupIntervalHours = 24;
        this.maxBackups = 5;
        this.searchResultLimit = 10;
        this.searchSimilarityThreshold = 0.1;
        this.fuzzyMatchThreshold = 0.7;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public int getBackupIntervalHours() {
        return backupIntervalHours;
    }

    public void setBackupIntervalHours(int backupIntervalHours) {
        this.backupIntervalHours = backupIntervalHours;
    }

    public int getMaxBackups() {
        return maxBackups;
    }

    public void setMaxBackups(int maxBackups) {
        this.maxBackups = maxBackups;
    }

    public int getSearchResultLimit() {
        return searchResultLimit;
    }

    public void setSearchResultLimit(int searchResultLimit) {
        this.searchResultLimit = searchResultLimit;
    }

    public double getSearchSimilarityThreshold() {
        return searchSimilarityThreshold;
    }

    public void setSearchSimilarityThreshold(double searchSimilarityThreshold) {
        this.searchSimilarityThreshold = searchSimilarityThreshold;
    }

    public double getFuzzyMatchThreshold() {
        return fuzzyMatchThreshold;
    }

    public void setFuzzyMatchThreshold(double fuzzyMatchThreshold) {
        this.fuzzyMatchThreshold = fuzzyMatchThreshold;
    }
}
