package tangzeqi.com.listener;

import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.project.MyProject;

@Slf4j
public class PluginListener implements DynamicPluginListener {
    @Override
    public void beforePluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        // 使用 Slf4j 日志替代 System.out.println，避免编码问题
        log.info("beforePluginLoaded pluginDescriptor = {}", pluginDescriptor);
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            MyProject.cache(project.getName()).sysMessage("beforePluginLoaded pluginDescriptor = " + pluginDescriptor);
        }
    }

    @Override
    public void beforePluginUnload(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        // 使用 Slf4j 日志替代 System.out.println，避免编码问题
        log.info("beforePluginUnload pluginDescriptor = {}, isUpdate = {}", pluginDescriptor, isUpdate);
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            MyProject.cache(project.getName()).sysMessage("beforePluginUnload pluginDescriptor = " + pluginDescriptor + ", isUpdate = " + isUpdate);
            MyProject.cache(project.getName()).shutDown();
        }
    }

    @Override
    public void pluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        // 使用 Slf4j 日志替代 System.out.println，避免编码问题
        log.info("pluginLoaded pluginDescriptor = {}", pluginDescriptor);
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            MyProject.cache(project.getName()).sysMessage("pluginLoaded pluginDescriptor = " + pluginDescriptor);
        }
    }

    @Override
    public void pluginUnloaded(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        // 使用 Slf4j 日志替代 System.out.println，避免编码问题
        log.info("pluginUnloaded pluginDescriptor = {}, isUpdate = {}", pluginDescriptor, isUpdate);
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            MyProject.cache(project.getName()).sysMessage("pluginUnloaded pluginDescriptor = " + pluginDescriptor + ", isUpdate = " + isUpdate);
        }
    }
}
