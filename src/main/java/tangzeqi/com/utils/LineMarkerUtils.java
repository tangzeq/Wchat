package tangzeqi.com.utils;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LineMarkerUtils {
    /**
     * key1 projectName
     * key2 lineNum
     * value RangeHighlighterEx
     */
    public static ConcurrentHashMap<String, Map<Integer, RangeHighlighterEx>> markers = new ConcurrentHashMap();
    volatile public static ConcurrentHashMap<String, Map<Integer, Set<String>>> tips = new ConcurrentHashMap();

    public static void addLineMarker(Editor editor, Integer rows, Icon icon) {
        Document document = editor.getDocument();
        Project project = editor.getProject();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return;
        TextRange range = new TextRange(document.getLineStartOffset(rows), document.getLineEndOffset(rows));
        LineMarkerInfo info = new LineMarkerInfo(
                psiFile.findElementAt(document.getLineStartOffset(rows)),
                range,
                icon,
                psiElement -> StringUtils.join(tips.get(project.getName()).get(rows), "\n"),//获取焦点的提示文本
                null,//点击事件
                (GutterIconRenderer.Alignment) GutterIconRenderer.Alignment.RIGHT
        );
        MarkupModelEx markupModel = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
        RangeHighlighterEx markerInfo = markupModel.addRangeHighlighterAndChangeAttributes(
                document.getLineStartOffset(rows),
                document.getLineEndOffset(rows),
                3000, null,
                HighlighterTargetArea.LINES_IN_RANGE, false, (markerEx) -> {
                    markerEx.setGutterIconRenderer(info.createGutterRenderer());
                    markerEx.setLineSeparatorColor(info.separatorColor);
                    markerEx.setLineSeparatorPlacement(info.separatorPlacement);
                    markerEx.putUserData(Key.create("LINE_MARKER_INFO"), info);
                });
        HashMap<Integer, RangeHighlighterEx> map = new HashMap<>();
        map.put(rows, markerInfo);
        markers.put(project.getName(), map);
        Map<Integer, Set<String>> tip =  new HashMap<>();
        tip.put(rows, new HashSet<String>());
        tips.put(project.getName(), tip);
    }

    public static void removeLineMarker(Editor editor, Integer rows) {
        Document document = editor.getDocument();
        Project project = editor.getProject();
        MarkupModelEx markupModel = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
        markupModel.removeHighlighter(markers.get(project.getName()).get(rows));
        markers.get(project.getName()).remove(rows);
        tips.get(project.getName()).remove(rows);
    }

    public static void changeTips(Editor editor, Integer rows, String tip) {
        tips.get(editor.getProject().getName()).get(rows).add(tip);
    }
}
