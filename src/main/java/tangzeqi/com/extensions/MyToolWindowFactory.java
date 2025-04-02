package tangzeqi.com.extensions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.panel.ChatPanel;
import tangzeqi.com.panel.ConfigPanel;
import tangzeqi.com.panel.HomePanel;
import tangzeqi.com.project.MyProject;

public class MyToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MyProject.init(project);
        MyProject.cache(project.getName()).toolWindow = toolWindow;
        toolWindow.getContentManager().addContent(HomePanel.content(project.getName()), 0);
        toolWindow.getContentManager().addContent(ConfigPanel.content(project.getName()), 1);
        toolWindow.getContentManager().addContent(ChatPanel.content(project.getName()), 2);
    }

}
