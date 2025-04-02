package tangzeqi.com.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.project.MyProject;

public class CodeLineToChat extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent an) {
        FileEditorManager manager = FileEditorManager.getInstance(an.getProject());
        String path = manager.getSelectedEditor().getFile().getPath();
        String base = MyProject.cache(an.getProject().getName()).project.getBaseDir().getPath();
        String name = path.replace(base, "");
        int line = manager.getSelectedTextEditor().getCaretModel().getLogicalPosition().line + 1;
        MyProject.cache(an.getProject().getName()).sendChat(name + ":" + line + "（点击跳转）");
        MyProject.cache(an.getProject().getName()).showContent("chat");
    }
}
