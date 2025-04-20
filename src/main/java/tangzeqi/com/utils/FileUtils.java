package tangzeqi.com.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tangzeqi.com.action.SynergyAction;
import tangzeqi.com.listener.MyDocumentListener;
import tangzeqi.com.project.MyProject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static void writeAtOffset(String projectName, String filePath,int statr, int end, String data) {
        Project project = MyProject.cache(projectName).project;
        String path = MyProject.cache(projectName).project.getBasePath();
        final String key = projectName + "-" + filePath;
        if(SynergyAction.sy.containsKey(key)) {
            MyDocumentListener listener = SynergyAction.sy.get(key);
            FileEditorManager manager = FileEditorManager.getInstance(project);
            Editor editor = null;
            for (FileEditor e : manager.getAllEditors()) {
                String path1 = e.getFile().getPath();
                String base = MyProject.cache(project.getName()).project.getBaseDir().getPath();
                String edFile = path1.replace(base, "");
                if(edFile.equalsIgnoreCase(filePath)) editor = (Editor) e;
            }
            if(editor != null) {
                editor.getDocument().removeDocumentListener(listener);
                editor.getDocument().replaceString(statr,end,data);
            }
        }
    }

}
