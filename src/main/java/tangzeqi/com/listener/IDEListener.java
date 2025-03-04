package tangzeqi.com.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.service.ChatService;

public class IDEListener implements ProjectManagerListener {
    @Override
    public void projectOpened(@NotNull Project project) {
        System.out.println("Wchat is executing the ProjectManagerListener Listener listener : projectOpened");
        ChatService.load(project);
        System.out.println("Wchat is executed the ProjectManagerListener Listener listener : projectOpened");
    }

    @Override
    public void projectClosing(@NotNull Project project) {
        System.out.println("Wchat is executing the ProjectManagerListener Listener listener : projectClosing");
        ChatService.shutDown();
        System.out.println("Wchat is executed the ProjectManagerListener Listener listener : projectClosing");
    }
}
