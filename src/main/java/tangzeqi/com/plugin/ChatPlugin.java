package tangzeqi.com.plugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.panel.ChatPanel;

import javax.swing.*;
import java.awt.*;

public class ChatPlugin implements ToolWindowFactory, ApplicationComponent {
    private static final Logger LOG = Logger.getInstance(ChatPlugin.class);
    private static ChatPlugin instance;
    public static ToolWindow toolWindow;
    public static Project project;

    public static ChatPlugin getInstance() {
        return instance;
    }

    @Override
    public void initComponent() {
        instance = this;
        LOG.info("ChatPlugin initialized");
    }

    @Override
    public void disposeComponent() {
        instance = null;
        LOG.info("ChatPlugin disposed");
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        this.project = project;
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        panel.add(new ChatPanel(), gbc);
        Content chat = ContentFactory.SERVICE.getInstance().createContent(panel, "Chat", false);
        toolWindow.getContentManagerIfCreated().addContent(chat);
    }
}
