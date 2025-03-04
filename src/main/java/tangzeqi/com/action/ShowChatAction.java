package tangzeqi.com.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import tangzeqi.com.panel.ChatPanel;
import tangzeqi.com.service.ChatService;

public class ShowChatAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ChatService.project = e.getProject();
        ChatPanel.register(ChatService.project);
    }
}
