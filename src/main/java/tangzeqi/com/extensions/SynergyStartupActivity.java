package tangzeqi.com.extensions;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.action.SynergyAction;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.stroge.SynergyMessage;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协同办公监听
 */
public class SynergyStartupActivity implements StartupActivity {
    private Project project;
    @Override
    public void runActivity(@NotNull Project project) {
        this.project = project;
        if (ObjectUtils.isEmpty(MyProject.cache(project.getName())))
            MyProject.init(project);
        EditorFactory.getInstance().addEditorFactoryListener(new MyEditorFactoryListener(), project);
    }
    private class MyEditorFactoryListener implements EditorFactoryListener {
        @Override
        public void editorReleased(@NotNull EditorFactoryEvent event) {
            if(event.getEditor().getDocument().getUserData(Key.findKeyByName("FILE_KEY")) != null) {
                String path = event.getEditor().getDocument().getUserData(Key.findKeyByName("FILE_KEY")).toString().replace("file://", "");
                String base = MyProject.cache(project.getName()).project.getBaseDir().getPath();
                String filePath = path.replace(base, "");
                event.getEditor().getProject().getName();
                final String key = project.getName() + "-" + filePath;
                SynergyAction.sy.remove(key);
            }
        }
    }
}
