package tangzeqi.com.extensions;

import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class SynInLay implements EditorCustomElementRenderer {

    private final String author;

    public SynInLay(String author) {
        this.author = author;
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        return 100; // 内联元素的宽度
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
        int textWidth = g.getFontMetrics().stringWidth(author + " :");
        g.setColor(JBColor.green);
        g.fillRect(targetRegion.x, targetRegion.y, textWidth + 20, targetRegion.height); // 20 是额外的填充
        g.setColor(JBColor.white);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        int textHeight = g.getFontMetrics().getHeight();
        int baselineY = targetRegion.y + (targetRegion.height - textHeight) / 2 + g.getFontMetrics().getAscent();
        g.drawString(author + " :", targetRegion.x + 10, baselineY);
    }
}
