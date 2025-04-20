package tangzeqi.com.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.project.MyProject;

public class IDEListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {
        System.out.println("Wchat is executing the ProjectManagerListener Listener listener : projectOpened");
        System.out.println("Wchat is executed the ProjectManagerListener Listener listener : projectOpened");
    }

    @Override
    public void projectClosing(@NotNull Project project) {
        System.out.println("Wchat is executing the ProjectManagerListener Listener listener : projectClosing");
        MyProject.cache(project.getName()).shutDown();
        System.out.println("Wchat is executed the ProjectManagerListener Listener listener : projectClosing");
    }
}
