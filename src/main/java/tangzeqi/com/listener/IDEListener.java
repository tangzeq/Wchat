package tangzeqi.com.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.project.MyProject;

@Slf4j
public class IDEListener implements ProjectManagerListener {

    @Override
    public void projectClosing(@NotNull Project project) {
        // 使用 Slf4j 日志替代 System.out.println，避免编码问题
        log.info("Wchat is executing the ProjectManagerListener Listener listener : projectClosing");
        try {
            var chatService = MyProject.cache(project.getName());
            if (chatService != null) {
                chatService.shutDown();
            }
        } catch (Throwable e) {
            log.error("Error shutting down chat service: {}", e.getMessage(), e);
        }
        log.info("Wchat is executed the ProjectManagerListener Listener listener : projectClosing");
    }
}
