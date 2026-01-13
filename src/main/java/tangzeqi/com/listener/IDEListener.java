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
        MyProject.cache(project.getName()).shutDown();
        log.info("Wchat is executed the ProjectManagerListener Listener listener : projectClosing");
    }
}
