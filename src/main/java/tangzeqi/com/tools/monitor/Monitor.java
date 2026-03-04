package tangzeqi.com.tools.monitor;

import java.util.List;

public interface Monitor {
    /**
     * 立即刷新所有数据
     */
    public void flushAll();

    /**
     * 刷新指定类型数据的图形展示
     *
     * @param type 类型
     */
    public List<Double> getOldData(String type);

    /**
     * 获取指定类型的数据
     * @param type 类型
     * @return
     */
    public Double getLastData(String type);

}