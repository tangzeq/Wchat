package tangzeqi.com.ui.memo;

import com.intellij.util.ui.WrapLayout;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import tangzeqi.com.tools.memo.MemoService;
import tangzeqi.com.tools.memo.server.LocalMemoService;
import tangzeqi.com.tools.memo.model.MemoItem;
import tangzeqi.com.ui.MyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MemoPanel extends JPanel implements MyPanel {
    private final String project;

    private JPanel memoPanel;
    private JTextField searchField;
    private JButton addButton;
    private JScrollPane scrollPane;
    private JLabel todoLabel;
    private JLabel completedLabel;
    public JPanel cards;

    private MemoService memoService;
    private List<MemoItem> memos;

    public MemoPanel(String project) {
        this.project = project;
        SwingUtilities.invokeLater(()->{
            init();
            memoService.timer(new Function() {
                @Override
                public Object call(Context context, List list) throws FunctionCallException {
                    memos = memoService.getAllMemos();
                    updateMemoContainer(memos);
                    updateStats();
                    return null;
                }
            });
        });
    }

    public MemoPanel() {
        this.project = null;
    }

    
    private void createUIComponents() {
        // TODO: place custom component creation code here
        WrapLayout layout = new WrapLayout(WrapLayout.LEFT, 10, 10); // 10px 水平和垂直间距
        cards = new JPanel(layout);
        cards.setOpaque(true);
        cards.setVisible(true);
        cards.setEnabled(true);
    }

    private void init() {
        memoService = new LocalMemoService();
        memos = memoService.getAllMemos();
        updateMemoContainer(memos);
        updateStats();
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddMemoDialog();
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterMemos();
            }
        });

        // 添加窗口大小变化监听器，实现自适应布局
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    updateMemoContainer(memos);
                });
            }
        });
        
        // 为scrollPane添加监听器，确保滚动面板大小变化时也能更新布局
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    updateMemoContainer(memos);
                });
            }
        });


    }

    private void showAddMemoDialog() {
        AddMemoDialog dialog = new AddMemoDialog();
        dialog.getUIFrame();
        dialog.save(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMemo(dialog.getMemo());
            }
        });
    }

    public void addMemo(MemoItem memo) {
        memoService.addMemo(memo);
        memos = memoService.getAllMemos();
        updateMemoContainer(memos);
        updateStats();
    }

    public void updateMemo(MemoItem memo) {
        memoService.updateMemo(memo);
        memos = memoService.getAllMemos();
        updateMemoContainer(memos);
        updateStats();
    }

    public void deleteMemo(String id) {
        memoService.deleteMemo(id);
        memos = memoService.getAllMemos();
        updateMemoContainer(memos);
        updateStats();
    }

    public void markAsCompleted(String id, boolean completed) {
        memoService.markAsCompleted(id, completed);
        memos = memoService.getAllMemos();
        updateMemoContainer(memos);
        updateStats();
    }
    
    @Override
    public boolean isShowing() {
        return memoPanel.isShowing();
    }

    private void filterMemos() {
        String searchText = searchField.getText().toLowerCase();
        List<MemoItem> items = memos.stream().filter(p -> p.getContent().toLowerCase().contains(searchText)).collect(Collectors.toList());
        updateMemoContainer(items);
    }
    private void updateMemoContainer(List<MemoItem> memos) {
        cards.removeAll();
        //按时间顺序排列未完成的
        memos.stream().filter(memo -> !memo.isCompleted()).sorted(Comparator.comparing(MemoItem::getReminderTime)).forEachOrdered(e->{
            cards.add(createMemoCard(e));
        });
        cards.revalidate();
        cards.repaint();
    }

    private JPanel createMemoCard(MemoItem memo) {
        MemoCard card = new MemoCard();
        card.text(
                memo.getContent(),
                Color.decode(memo.getCardColor()),
                Color.decode(memo.getTextColor())
        );
        card.setTime(memo.getReminderTime());
        card.setCompleted(memo.isCompleted());
        card.buttons(
                e -> {
                    markAsCompleted(memo.getId(), !memo.isCompleted());
                },
                e -> {
                    AddMemoDialog dialog = new AddMemoDialog();
                    dialog.getUIFrame();
                    dialog.setMemo(memo);
                    dialog.save(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            updateMemo(dialog.getMemo());
                        }
                    });
                },
                e -> {
                    if (JOptionPane.showConfirmDialog(this, "确定要删除这个任务吗？", "删除任务", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        deleteMemo(memo.getId());
                    }
                }
        );
        return card.getUIComponents();
    }

    private void updateStats() {
        int todoCount = 0;
        int completedCount = 0;
        int todayCompletedCount = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());

        for (MemoItem memo : memos) {
            if (!memo.isCompleted()) {
                todoCount++;
            } else {
                completedCount++;
                if (memo.getReminderTime().startsWith(today)) {
                    todayCompletedCount++;
                }
            }
        }

        todoLabel.setText("待办任务：" + todoCount);
        completedLabel.setText("今日完成：" + todayCompletedCount + "/" + completedCount);
    }

    @Override
    public JComponent getComponent(String project) {
        return new MemoPanel(project).memoPanel;
    }
    
    public JPanel getMemoPanel() {
        return memoPanel;
    }
}