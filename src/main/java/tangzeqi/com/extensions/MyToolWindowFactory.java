package tangzeqi.com.extensions;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerListener;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.panel.ChatPanel;
import tangzeqi.com.panel.ConfigPanel;
import tangzeqi.com.panel.HomePanel;
import tangzeqi.com.service.ChatService;

import javax.swing.*;
import java.awt.*;

public class MyToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ChatService.project = project;
        ChatService.toolWindow = toolWindow;
        toolWindow.getContentManager().addContent(HomePanel.content(),0);
        toolWindow.getContentManager().addContent(ConfigPanel.content(),1);
        toolWindow.getContentManager().addContent(ChatPanel.content(),2);
    }

}
