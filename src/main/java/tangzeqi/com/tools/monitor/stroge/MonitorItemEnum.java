package tangzeqi.com.tools.monitor.stroge;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum MonitorItemEnum {
    SYS_CPU("systemCpu", "操作系统CPU使用率", new Color(220, 20, 60)),      // 猩红色
    SYS_MEM("systemMemory", "操作系统内存使用率", new Color(0, 128, 0)),     // 绿色
    SYS_DISK("systemDisk", "操作系统磁盘使用率", new Color(0, 0, 255)),     // 蓝色
    SYS_UP_SPEED("systemUpload", "操作系统上传网速", new Color(255, 140, 0)), // 深橙色
    SYS_DOWN_SPEED("systemDownload", "操作系统下载网速", new Color(0, 191, 255)), // 深天蓝
    ;
    private final String code;
    private final String desc;
    private final Color color;

    private Double lastData = 0.00;
    private final List<Double> oldData = new ArrayList<>();

    MonitorItemEnum(String code, String desc, Color color) {
        this.code = code;
        this.desc = desc;
        this.color = color;
        // 初始化300个默认的0值
        for (int i = 0; i < 300; i++) {
            oldData.add(0.0);
        }
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return this.name();
    }

    public Color getColor() {
        return color;
    }

    public Double getLastData() {
        return lastData;
    }

    public List<Double> getOldData() {
        return oldData;
    }

    public synchronized void addData(Double value) {
        this.lastData = value;
        //最多存储300个点
        if(oldData.size() >= 300) {
            oldData.remove(0);
        }
        oldData.add(value);
    }

}
