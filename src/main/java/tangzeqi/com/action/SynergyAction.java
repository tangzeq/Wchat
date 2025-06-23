package tangzeqi.com.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.extensions.SynInLay;
import tangzeqi.com.listener.MyDocumentListener;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.utils.LineMarkerUtils;
import tangzeqi.com.utils.NetUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SynergyAction extends AnAction {
    public static volatile ConcurrentHashMap<String, MyDocumentListener> sy = new ConcurrentHashMap<>();

    @Override
    public void actionPerformed(@NotNull AnActionEvent an) {
        Project project = an.getProject();
        Editor editor = (Editor) an.getDataContext().getData("editor");
        String path = null;
        try {
            path = editor.getDocument().getUserData(Key.findKeyByName("FILE_KEY")).toString().replace("file://", "");
        } catch (Throwable e) {
            path = (String) an.getDataContext().getData("virtualFile").toString().replace("file://", "");
        }
        if (editor == null) {
            Messages.showInfoMessage("未获取到有效的编辑器！", "协同编辑");
        } else if (editor.getDocument() == null) {
            Messages.showInfoMessage("未获取到有效文档", "协同编辑");
        } else if (ObjectUtils.isEmpty(path)) {
            Messages.showInfoMessage("正在加载模组中，请稍后打开！", "协同编辑");
        } else {
            String base = MyProject.cache(project.getName()).project.getBaseDir().getPath();
            String filePath = path.replace(base, "");
            final String key = project.getName() + "-" + filePath;
            if (sy.containsKey(key)) {
                int i = Messages.showOkCancelDialog("当前文件正在协同编辑中，请选择？", "协同编辑", "继续协同", "取消协同", Messages.getQuestionIcon());
                if (i == Messages.OK) {
                } else {
                    editor.getDocument().removeDocumentListener(sy.get(key));
                    sy.remove(key);
                    @NotNull List<Inlay<?>> inlays = editor.getInlayModel().getBlockElementsInRange(0, editor.getDocument().getLineEndOffset(editor.getDocument().getLineCount() - 1));
                    for (Inlay inlay : inlays) {
                        if (inlay.getRenderer() instanceof SynInLay) {
                            inlay.dispose();
                        }
                    }
                }
            } else {
                int i = Messages.showOkCancelDialog("当前文件未协同编辑，请选择？", "协同编辑", "继续协同", "取消协同", Messages.getQuestionIcon());
                if (i == Messages.OK) {
                    final String listCode = NetUtils.mac() + "-" + UUID.randomUUID().toString();
                    final MyDocumentListener listener = new MyDocumentListener(filePath, project.getName(), listCode);
                    MyProject.cache(project.getName()).synListener = listener;
                    editor.getDocument().addDocumentListener(listener);
                    sy.put(key, listener);
                } else {
                }
            }
        }
    }
}
