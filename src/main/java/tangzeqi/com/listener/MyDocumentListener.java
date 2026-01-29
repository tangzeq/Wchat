package tangzeqi.com.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tools.ant.util.DateUtils;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.action.SynergyAction;
import tangzeqi.com.extensions.SynInLay;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.tools.chat.stroge.SynergyMessage;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MyDocumentListener implements DocumentListener {
    private static final TextAttributes textAttributes = new TextAttributes();

    public String listCode;

    private long oldTimeStamp = -1l;
    private final String filePath;
    private final String project;
    private final MyDocumentListener listener;
    //    private final SynergyMessage oldMessage;
//    private final SynergyMessage newMessage;
    private volatile SynergyMessage message;

    private final ConcurrentHashMap<String, Long> changed = new ConcurrentHashMap<>();

    public MyDocumentListener(@NotNull String filePath, @NotNull String project, @NotNull String listCode) {
        this.filePath = filePath;
        this.listener = this;
        this.project = project;
        this.listCode = listCode;
        textAttributes.setForegroundColor(JBColor.GREEN);
    }

    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
//        message(oldMessage, event.getOldFragment().toString(), event);
//        message(newMessage, event.getNewFragment().toString(), event);
//        /**
//         * UPD
//         */
//        if (JSON.toJSONString(oldMessage).getBytes(StandardCharsets.UTF_8).length > 1024 || JSON.toJSONString(newMessage).getBytes(StandardCharsets.UTF_8).length > 1024) {
//            undo(event, listener);
//        } else {
//            oldTimeStamp = event.getOldTimeStamp();
//        }
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
//        if (event.getOldTimeStamp() == oldTimeStamp) {
//            log.info("{} :olded ={} to {} char:{}", filePath, event.getOffset(), event.getMoveOffset(), event.getOldFragment().toString());
        log.info("{} : {} {}", project, event, event.getOldTimeStamp());
//            log.info("oldTimeStamp ={}", oldTimeStamp);
        MyProject.cache(project).customerHandler.send(message(event.getNewFragment().toString(), event));
//        }
    }

    private SynergyMessage message(String str, @NotNull DocumentEvent event) {
        SynergyMessage message = new SynergyMessage();
        message.setProject(project);
        message.setUnicode(listCode);
        message.setFile(filePath);
        message.setStr(str);
        message.setName(MyProject.cache(project).userName);
        message.setStartOffset(event.getOffset());
        message.setEndOffset(event.getMoveOffset());
        message.setIndex(event.getOldTimeStamp());
        message.setOldLength(event.getOldLength());
        message.setNewLength(event.getNewLength());
        message.setOldTimeStamp(event.getOldTimeStamp());
        return message;
    }

    synchronized private void undo(@NotNull DocumentEvent event, @NotNull MyDocumentListener listener) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Messages.showMessageDialog("协同操作超出 1024 字节限制，请拆分操作。\n" + event.getOldLength() + "-->" + event.getNewLength(), "操作限制", Messages.getWarningIcon());
            WriteCommandAction.runWriteCommandAction(MyProject.cache(project).project, () -> {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    final Document document = event.getDocument();
                    document.removeDocumentListener(listener);
                    document.replaceString(event.getOffset(), Math.min(event.getOffset() + event.getNewLength(), event.getDocument().getTextLength()), event.getOldFragment());
                    document.addDocumentListener(listener);
                });
            });
        });
    }

    synchronized public static void syne(String projectName, SynergyMessage message) {
        try {
            final Project project = MyProject.cache(projectName).project;
            final SynergyMessage syn = message;
            final String filePath = syn.getFile();
            final String key = projectName + "-" + filePath;
            if (SynergyAction.sy.containsKey(key)) {
                MyDocumentListener listener = SynergyAction.sy.get(key);
                FileEditorManager manager = FileEditorManager.getInstance(project);
                Editor editor = null;
                for (FileEditor e : manager.getAllEditors()) {
                    String path1 = e.getFile().getPath();
                    String base = MyProject.cache(project.getName()).project.getBaseDir().getPath();
                    String edFile = path1.replace(base, "");
                    if (edFile.equalsIgnoreCase(filePath) && e instanceof TextEditor)
                        editor = ((TextEditor) e).getEditor();
                }
                if (editor != null) {
                    final Editor finalEditor = editor;
                    WriteCommandAction.runWriteCommandAction(MyProject.cache(projectName).project, () -> {
                        ApplicationManager.getApplication().runWriteAction(() -> {
                            if (
                                    !MyProject.cache(projectName).synListener.listCode.equalsIgnoreCase(syn.getUnicode())
                            ) {
                                MyProject.cache(projectName).synListener.changed.put(syn.getUnicode(), syn.getOldTimeStamp());
//                                System.out.println(projectName + ":" + syn);
                                finalEditor.getDocument().removeDocumentListener(listener);
                                String str = syn.getStr();
//                                if (syn.getOldLength() <= 0) {
//                                    finalEditor.getDocument().insertString(syn.getStartOffset(),syn.getEndOffset(), str);
//                                } else if (syn.getNewLength() <= 0) {
//                                    finalEditor.getDocument().deleteString(syn.getStartOffset(), Math.min(syn.getEndOffset() + syn.getOldLength(), finalEditor.getDocument().getTextLength()));
//                                } else {
                                finalEditor.getDocument().replaceString(syn.getStartOffset(), Math.min(syn.getEndOffset() + syn.getOldLength(), finalEditor.getDocument().getTextLength()), str);
//                                }
                                finalEditor.getDocument().addDocumentListener(listener);
                            }
                            addInlay(finalEditor, syn);
                        });
                    });
                }
            }

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void addInlay(@NotNull Editor editor, @NotNull SynergyMessage syn) {
        int offset = editor.getDocument().getLineStartOffset(editor.getDocument().getLineNumber(syn.getStartOffset()));
//        log.info("line = {}, offset = {}, syn = {}", editor.getDocument().getLineNumber(syn.getStartOffset()), offset, syn.getStartOffset());
        String tip = syn.getName() + "(" + DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss") + ")";
        @NotNull List<Inlay<?>> inlays = editor.getInlayModel().getBlockElementsInRange(0, editor.getDocument().getLineEndOffset(editor.getDocument().getLineCount() - 1));
        for (Inlay<?> inlay : inlays) {
            if (editor.getDocument().getLineNumber(inlay.getOffset()) == editor.getDocument().getLineNumber(offset) && inlay.getRenderer() instanceof SynInLay) inlay.dispose();
        }
        editor.getInlayModel().addBlockElement(offset, true, true, 0, new SynInLay(tip));
    }
}
