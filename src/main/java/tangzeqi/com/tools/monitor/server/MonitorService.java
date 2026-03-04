package tangzeqi.com.tools.monitor.server;

import tangzeqi.com.tools.monitor.Monitor;
import tangzeqi.com.tools.monitor.stroge.MonitorItemEnum;
import tangzeqi.com.utils.SystemUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorService implements Monitor {

    private final String project;
    private static Map<String, Color> colorData = new HashMap<>();
    private static ConcurrentHashMap<String, Double> lastData = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<Double>> oldData = new ConcurrentHashMap<>();

    public MonitorService(String project) {
        this.project = project;
    }



    @Override
    public void flushAll() {
        MonitorItemEnum.SYS_CPU.addData(SystemUtils.getSystemCpuUsage());
        MonitorItemEnum.SYS_MEM.addData(SystemUtils.getSystemMemoryUsage());
        MonitorItemEnum.SYS_DISK.addData(SystemUtils.getSystemDiskUsage());
        MonitorItemEnum.SYS_UP_SPEED.addData(SystemUtils.getUploadSpeedInBytes());
        MonitorItemEnum.SYS_DOWN_SPEED.addData(SystemUtils.getDownloadSpeedInBytes());
    }

    @Override
    public List<Double> getOldData(String type) {
        return MonitorItemEnum.valueOf(type).getOldData();
    }

    @Override
    public Double getLastData(String type) {
        return MonitorItemEnum.valueOf(type).getLastData();
    }
}