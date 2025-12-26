package tangzeqi.com.extensions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.ui.Wchat;

public class MyToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MyProject.init(project);
        MyProject.cache(project.getName()).toolWindow = toolWindow;
        toolWindow.getContentManager().addContent(ContentFactory.getInstance().createContent(new Wchat(project.getName()).$$$getRootComponent$$$(),"home",false), 0);
//        toolWindow.getContentManager().addContent(HomePanel.content(project.getName()), 0);
//        toolWindow.getContentManager().addContent(ConfigPanel.content(project.getName()), 1);
//        toolWindow.getContentManager().addContent(ChatPanel.content(project.getName()), 2);
        // 设置工具窗口的初始大小
        toolWindow.setAutoHide(false);
    }

}
