package tangzeqi.com.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.panel.ChatPanel;
import tangzeqi.com.service.ChatService;

public class CodeLineToChat extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent an) {
        FileEditorManager manager = FileEditorManager.getInstance(an.getProject());
        String path = manager.getSelectedEditor().getFile().getPath();
        String base = ChatService.project.getBaseDir().getPath();
        String name = path.replace(base, "");
        int line = manager.getSelectedTextEditor().getCaretModel().getLogicalPosition().line + 1;
        ChatService.sendChat(name + ":" + line + "（点击跳转）");
        ChatService.showContent("chat");
    }
}
