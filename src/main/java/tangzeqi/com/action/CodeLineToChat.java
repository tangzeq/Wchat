package tangzeqi.com.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.service.ChatService;

public class CodeLineToChat extends AnAction {

    public static volatile Project project;
    @Override
    public void actionPerformed(@NotNull AnActionEvent an) {
        project = an.getProject();
        if(ObjectUtils.isEmpty(ShowChatAction.project) || ObjectUtils.isEmpty(ChatService.chat)) {
            ShowChatAction.project = project;
            ShowChatAction.openChat();
        } else ShowChatAction.openChat();
        FileEditorManager manager = FileEditorManager.getInstance(an.getProject());
        String path = manager.getSelectedEditor().getFile().getPath();
        String base = project.getBaseDir().getPath();
        String name = path.replace(base,"");
        int line = manager.getSelectedTextEditor().getCaretModel().getLogicalPosition().line+1;
        ChatService.sendChat(name+":"+line+"（点击跳转）");
    }
}
