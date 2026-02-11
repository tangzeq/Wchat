package tangzeqi.com.extensions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.ui.MyPanel;
import tangzeqi.com.ui.broser.BrowserPanel;
import tangzeqi.com.ui.chat.ChatRoomPanel;
import tangzeqi.com.ui.folder.FolderPanel;
import tangzeqi.com.ui.mind.MindPanel;
import tangzeqi.com.ui.monitor.MonitorPanel;
import tangzeqi.com.ui.tools.ToolsPanel;

public class MyToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MyProject.init(project);
        MyProject.cache(project.getName()).toolWindow = toolWindow;
        addContent(project,toolWindow,ChatRoomPanel.class,"聊天室");
        addContent(project,toolWindow,FolderPanel.class,"文件夹");
        addContent(project,toolWindow,BrowserPanel.class,"浏览器");
        addContent(project,toolWindow,ToolsPanel.class,"工具栏");
        addContent(project,toolWindow,MindPanel.class,"记忆库");
        addContent(project,toolWindow,MonitorPanel.class,"监控面板");
        // 设置工具窗口的初始大小
        toolWindow.setAutoHide(false);
    }

    private void addContent(@NotNull Project project, @NotNull ToolWindow toolWindow, @NotNull Class<? extends MyPanel> t, @NotNull String title) {
        try {
            toolWindow.getContentManager().addContent(
                    ContentFactory.getInstance().createContent(
                            t.newInstance().getComponent(project.getName()),
                            title,
                            false
                    )
            );
        } catch (Exception e) {
            System.err.println("加载【"+title+"】组件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
